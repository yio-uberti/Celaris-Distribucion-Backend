package com.api.controlador;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.entidad.PagoDeuda;
import com.api.entidad.clientes;
import com.api.repositorio.repoClientes;
import com.api.repositorio.repoPagoDeuda;
import com.api.servicio.DeudaServicio;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/control/deudas")
public class conDeuda {

	@Autowired
	private DeudaServicio deudaServicio;
	@Autowired
	private repoClientes clienteRepository;
	@Autowired
	private repoPagoDeuda pagoDeudaRepository;

	@Autowired
	private UserRepository userRepository;
	
	private Long getTenantId(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(uid).orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
				.getTenant().getId();
	}
	
	@GetMapping
	public ResponseEntity<List<clientes>> getDeudores(HttpServletRequest request) {
		return ResponseEntity.ok(deudaServicio.getDeudores(request));
	}

	@GetMapping("/{idCliente}/historial")
	public ResponseEntity<?> getHistorial(HttpServletRequest request, @PathVariable Integer idCliente) {
	    Long tenantId = getTenantId(request);
	    
	    clientes cliente = clienteRepository
	        .findByIdClienteAndTenantId(idCliente, tenantId)
	        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
	    
	    List<PagoDeuda> pagos = pagoDeudaRepository
	        .findAllByClienteIdClienteAndTenantId(idCliente, tenantId);

	    BigDecimal totalPagado = pagos.stream()
	        .map(PagoDeuda::getMonto)
	        .reduce(BigDecimal.ZERO, BigDecimal::add);

	    return ResponseEntity.ok(Map.of(
	        "nombreCliente", cliente.getNombreCliente(),
	        "saldoActual",   cliente.getSaldoDeudor(),
	        "totalPagado",   totalPagado,
	        "pagos",         pagos
	    ));
	}

	@PatchMapping("/pagar")
	public ResponseEntity<clientes> pagar(HttpServletRequest request, @RequestBody PagoDeudaRequest req) {
		return ResponseEntity.ok(deudaServicio.registrarPago(request, req));
	}
}
