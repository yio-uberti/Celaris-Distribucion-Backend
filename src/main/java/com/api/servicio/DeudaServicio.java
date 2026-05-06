package com.api.servicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.controlador.PagoDeudaRequest;
import com.api.entidad.PagoDeuda;
import com.api.entidad.TipoPago;
import com.api.entidad.clientes;
import com.api.repositorio.repoClientes;
import com.api.repositorio.repoPagoDeuda;
import com.api.repositorio.repoTipoPago;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class DeudaServicio {
	@Autowired
	private repoClientes clienteRepository;
	@Autowired
	private repoPagoDeuda pagoDeudaRepository;
	@Autowired
	private repoTipoPago tipoPagoRepository;
	@Autowired
	private UserRepository userRepository;

	private Long getTenantId(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(uid).orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
				.getTenant().getId();
	}

	// GET deudores
	public List<clientes> getDeudores(HttpServletRequest request) {
		return (List<clientes>) clienteRepository.findAllByTenantIdAndSaldoDeudorGreaterThan(getTenantId(request),
				BigDecimal.ZERO);
	}

	// GET historial de pagos de un cliente
	public List<PagoDeuda> getHistorialPagos(HttpServletRequest request, Integer idCliente) {
	    List<PagoDeuda> historial = pagoDeudaRepository
	        .findAllByClienteIdClienteAndTenantId(idCliente, getTenantId(request));
	    return historial != null ? historial : new ArrayList<>();
	}

	// PATCH registrar pago
	@Transactional
	public clientes registrarPago(HttpServletRequest request, PagoDeudaRequest req) {
		Long tenantId = getTenantId(request);

		clientes cliente = clienteRepository.findByIdClienteAndTenantId(req.getIdCliente(), tenantId)
				.orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

		TipoPago tipoPago = tipoPagoRepository.findById(req.getIdTipoPago())
				.orElseThrow(() -> new RuntimeException("Tipo de pago no encontrado"));

		// Calcular nuevo saldo — mínimo 0
		BigDecimal nuevoSaldo = cliente.getSaldoDeudor().subtract(req.getMonto());
		cliente.setSaldoDeudor(nuevoSaldo.max(BigDecimal.ZERO));
		clienteRepository.save(cliente);

		// Guardar en historial
		pagoDeudaRepository.save(PagoDeuda.builder().cliente(cliente).monto(req.getMonto()).tipoPago(tipoPago)
				.fecha(LocalDateTime.now()).tenantId(tenantId).build());

		return cliente;
	}
}
