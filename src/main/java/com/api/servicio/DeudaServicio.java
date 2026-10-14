package com.api.servicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.controlador.PagoDeudaRequest;
import com.api.entidad.DetalleVenta;
import com.api.entidad.PagoDeuda;
import com.api.entidad.TipoPago;
import com.api.entidad.VentaTipoPago;
import com.api.entidad.Ventas;
import com.api.entidad.clientes;
import com.api.repositorio.repoClientes;
import com.api.repositorio.repoPagoDeuda;
import com.api.repositorio.repoTipoPago;
import com.api.repositorio.repoVentas;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class DeudaServicio {
	@Autowired private repoClientes clienteRepository;
	@Autowired private repoPagoDeuda pagoDeudaRepository;
	@Autowired private repoTipoPago tipoPagoRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private repoVentas ventaRepository;
	@Autowired private PlanLimiteService planLimiteService;

	private Long getTenantId(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(uid)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"))
				.getTenant().getId();
	}

	// Contabilidad es Premium: se exige acá, no solo en la app
	private Long tenantPremium(HttpServletRequest request) {
		Long tenantId = getTenantId(request);
		if (!planLimiteService.tenantEsPremium(tenantId))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Función exclusiva del plan Premium");
		return tenantId;
	}

	private clientes buscarCliente(Integer id, Long tenantId) {
		return clienteRepository.findByIdClienteAndTenantId(id, tenantId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
	}

	public List<clientes> getDeudores(HttpServletRequest request) {
		Long tenantId = tenantPremium(request);
		return (List<clientes>) clienteRepository.findAllByTenantIdAndSaldoDeudorGreaterThan(tenantId, BigDecimal.ZERO);
	}

	public Map<String, Object> getHistorial(HttpServletRequest request, Integer idCliente) {
		Long tenantId = tenantPremium(request);
		clientes cliente = buscarCliente(idCliente, tenantId);
		List<PagoDeuda> pagos = pagoDeudaRepository
				.findAllByClienteIdClienteAndTenantIdOrderByFechaDesc(idCliente, tenantId);
		BigDecimal totalPagado = pagos.stream().map(PagoDeuda::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("nombreCliente", cliente.getNombreCliente());
		out.put("saldoActual", cliente.getSaldoDeudor() != null ? cliente.getSaldoDeudor() : BigDecimal.ZERO);
		out.put("totalPagado", totalPagado);
		out.put("pagos", pagos);
		return out;
	}

	@Transactional
	public clientes registrarPago(HttpServletRequest request, PagoDeudaRequest req) {
		Long tenantId = tenantPremium(request);

		if (req.getMonto() == null || req.getMonto().signum() <= 0)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monto inválido");

		clientes cliente = buscarCliente(req.getIdCliente(), tenantId);
		TipoPago tipoPago = tipoPagoRepository.findById(req.getIdTipoPago())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de pago no encontrado"));

		BigDecimal saldo = cliente.getSaldoDeudor() != null ? cliente.getSaldoDeudor() : BigDecimal.ZERO;
		if (req.getMonto().compareTo(saldo) > 0)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto supera la deuda del cliente");

		cliente.setSaldoDeudor(saldo.subtract(req.getMonto()));
		clienteRepository.save(cliente);

		pagoDeudaRepository.save(PagoDeuda.builder().cliente(cliente).monto(req.getMonto())
				.tipoPago(tipoPago).fecha(LocalDateTime.now()).tenantId(tenantId).build());
		return cliente;
	}

	// Mercadería que explica la deuda actual (las ventas más nuevas con saldo)
	@Transactional(readOnly = true)
	public Map<String, Object> getMercaderiaPendiente(HttpServletRequest request, Integer idCliente) {
		Long tenantId = tenantPremium(request);
		clientes cliente = buscarCliente(idCliente, tenantId);
		BigDecimal saldo = cliente.getSaldoDeudor() != null ? cliente.getSaldoDeudor() : BigDecimal.ZERO;
		BigDecimal restante = saldo;

		List<Map<String, Object>> ventasOut = new ArrayList<>();
		for (Ventas v : ventaRepository.findParaMercaderia(tenantId, idCliente)) {
			if (restante.signum() <= 0) break;

			BigDecimal pagado = v.getPagos().stream().map(VentaTipoPago::getMonto)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal deudaVenta = v.getTotal().subtract(pagado);
			if (deudaVenta.signum() <= 0) continue;

			BigDecimal pendienteVenta = deudaVenta.min(restante);
			restante = restante.subtract(pendienteVenta);

			List<Map<String, Object>> items = new ArrayList<>();
			for (DetalleVenta d : v.getDetalles()) {
				Map<String, Object> it = new LinkedHashMap<>();
				it.put("nombre", d.getProducto() != null ? d.getProducto().getNombreProducto() : "Producto eliminado");
				it.put("cantidad", d.getCantidad());
				it.put("precioUnitario", d.getPrecio_unitario());
				it.put("subtotal", d.getSubtotal());
				items.add(it);
			}

			Map<String, Object> venta = new LinkedHashMap<>();
			venta.put("idVenta", v.getIdVenta());
			venta.put("fecha", v.getFecha_hora());
			venta.put("descuento", v.getDescuento() != null ? v.getDescuento() : 0);
			venta.put("total", v.getTotal());
			venta.put("pagado", pagado);
			venta.put("pendiente", pendienteVenta);
			venta.put("productos", items);
			ventasOut.add(venta);
		}

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("nombreCliente", cliente.getNombreCliente());
		out.put("saldoActual", saldo);
		out.put("ventas", ventasOut);
		return out;
	}
}