package com.api.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.controlador.VentaRequest;
import com.api.controlador.top5productos;
import com.api.entidad.DetalleVenta;
import com.api.entidad.TipoPago;
import com.api.entidad.VentaTipoPago;
import com.api.entidad.Ventas;
import com.api.entidad.clientes;
import com.api.entidad.productos;
import com.api.repositorio.repoClientes;
import com.api.repositorio.repoProducto;
import com.api.repositorio.repoTipoPago;
import com.api.repositorio.repoVentas;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class VentaServicio {

	@Autowired
	private repoVentas ventaRepository;
	@Autowired
	private repoProducto productoRepository;
	@Autowired
	private ClienteServicio clienteService;
	@Autowired
	private repoTipoPago tipoPagoRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private repoClientes clienteRepository;
	
	// Exponé getTenantId como público
	public Long getTenantIdPublic(HttpServletRequest request) {
	    return getTenantId(request);
	}
	
	
	private Long getTenantId(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(uid).orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
				.getTenantId();
	}

	// GET — traer todas las ventas del tenant
	public List<Ventas> getAll(HttpServletRequest request) {
		return ventaRepository.findAllByTenantId(getTenantId(request));
	}

	// GET - trae las ventas reservadas
	public List<Ventas> getReservadas(HttpServletRequest request) {
		return ventaRepository.findAllByTenantIdAndEstado(getTenantId(request), "RESERVADA");
	}

	// GET - top 5 productos del dia
	public List<top5productos> getTop5(HttpServletRequest request) {
		return ventaRepository.listaTop5(LocalDate.now(), getTenantId(request));
	}

	// GET -historial de ventas segun tipo de busqueda
	public List<Ventas> getHistorial(HttpServletRequest request, LocalDate fecha, String tipoPago) {
		Long tenantId = getTenantId(request);
		if (tipoPago == null || tipoPago.equals("todos")) {
			return ventaRepository.findByFecha(tenantId, fecha);
		}
		return ventaRepository.findByFechaAndTipoPago(tenantId, fecha, tipoPago);
	}

	// POST — crear venta completa con detalles y pagos
	@Transactional
	public Ventas create(HttpServletRequest request, VentaRequest req) {
		Long tenantId = getTenantId(request);
		clientes cliente = clienteService.findOrCreate(req.getNombreCliente(), tenantId);

		LocalDate fechaEntrega = req.getFechaEntrega();
		LocalDate hoy = LocalDate.now();
		boolean esReservada = fechaEntrega != null && fechaEntrega.isAfter(hoy);

		// Crear la venta base
		Ventas venta = new Ventas();
		venta.setCliente(cliente);
		venta.setFecha_hora(LocalDateTime.now());
		venta.setFechaEntrega(fechaEntrega != null ? fechaEntrega : hoy);
		venta.setEstado(esReservada ? "RESERVADA" : "REGISTRADA");
		venta.setTenantId(tenantId);

		// Armar detalles y calcular total
		List<DetalleVenta> detalles = new ArrayList<>();
		BigDecimal total = BigDecimal.ZERO;
		for (VentaRequest.DetalleRequest d : req.getDetalles()) {
			productos producto = productoRepository.findByIdProdAndTenantId(d.getIdProducto(), tenantId)
					.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

			BigDecimal subtotal = producto.getPrecioActual().multiply(BigDecimal.valueOf(d.getCantidad()));

			detalles.add(DetalleVenta.builder().venta(venta).producto(producto).cantidad(d.getCantidad())
					.precio_unitario(producto.getPrecioActual()).subtotal(subtotal).build());

			total = total.add(subtotal);
		}

		venta.setTotal(total);
		venta.setDetalles(detalles);

		// Pagos — solo si NO es reservada
		if (!esReservada && req.getPagos() != null && !req.getPagos().isEmpty()) {
			List<VentaTipoPago> pagos = new ArrayList<>(req.getPagos().stream().map(p -> {
				TipoPago tipoPago = tipoPagoRepository.findById(p.getIdTipoPago())
						.orElseThrow(() -> new RuntimeException("Tipo de pago no encontrado"));
				return VentaTipoPago.builder().venta(venta).tipoPago(tipoPago).monto(p.getMonto()).build();
			}).toList());

			venta.setPagos(pagos);

			// Calcular deuda
			BigDecimal totalPagado = req.getPagos().stream().map(p -> p.getMonto()).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			BigDecimal deuda = total.subtract(totalPagado);
			if (deuda.compareTo(BigDecimal.ZERO) > 0) {
				cliente.setSaldoDeudor(
						(cliente.getSaldoDeudor() != null ? cliente.getSaldoDeudor() : BigDecimal.ZERO).add(deuda));
				clienteRepository.save(cliente);
			}
		}

		return ventaRepository.save(venta);
	}

	// POST - para confirmar entregas de compras reservadas y registro de pago
	@Transactional
	public Ventas confirmarEntrega(Integer id, HttpServletRequest request, VentaRequest.ConfirmarEntregaRequest req) {
		Long tenantId = getTenantId(request);
		Ventas venta = ventaRepository.findByIdVentaAndTenantId(id, tenantId)
				.orElseThrow(() -> new RuntimeException("Venta no encontrada"));

		// Registrar pagos
		List<VentaTipoPago> pagos = new ArrayList<>(req.getPagos().stream().map(p -> {
			TipoPago tipoPago = tipoPagoRepository.findById(p.getIdTipoPago())
					.orElseThrow(() -> new RuntimeException("Tipo de pago no encontrado"));
			return VentaTipoPago.builder().venta(venta).tipoPago(tipoPago).monto(p.getMonto()).build();
		}).toList());
		venta.setPagos(pagos);
		venta.setEstado("ENTREGADA");

		// Calcular deuda
		BigDecimal totalPagado = req.getPagos().stream().map(VentaRequest.PagoRequest::getMonto).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal deuda = venta.getTotal().subtract(totalPagado);

		if (deuda.compareTo(BigDecimal.ZERO) > 0) {
			clientes cliente = venta.getCliente();
			cliente.setSaldoDeudor(
					(cliente.getSaldoDeudor() != null ? cliente.getSaldoDeudor() : BigDecimal.ZERO).add(deuda));
			clienteRepository.save(cliente);
		}

		return ventaRepository.save(venta);
	}
	
//	PUT para editar una venta agregar o quitra productos
	@Transactional
	public Ventas editarProductos(Integer id, List<VentaRequest.DetalleRequest> detalles, HttpServletRequest request) {
	    Long tenantId = getTenantId(request);

	    Ventas venta = ventaRepository.findByIdVentaAndTenantId(id, tenantId)
	        .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

	    // Recalcular detalles y total
	    List<DetalleVenta> nuevosDetalles = new ArrayList<>();
	    BigDecimal total = BigDecimal.ZERO;

	    for (VentaRequest.DetalleRequest d : detalles) {
	        productos producto = productoRepository.findByIdProdAndTenantId(d.getIdProducto(), tenantId)
	            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

	        BigDecimal subtotal = producto.getPrecioActual()
	            .multiply(BigDecimal.valueOf(d.getCantidad()));

	        nuevosDetalles.add(DetalleVenta.builder()
	            .venta(venta)
	            .producto(producto)
	            .cantidad(d.getCantidad())
	            .precio_unitario(producto.getPrecioActual())
	            .subtotal(subtotal)
	            .build());

	        total = total.add(subtotal);
	    }

	    venta.getDetalles().clear();
	    venta.getDetalles().addAll(nuevosDetalles);
	    venta.setTotal(total);

	    return ventaRepository.save(venta);
	}
}
