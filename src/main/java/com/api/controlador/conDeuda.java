package com.api.controlador;

import java.util.List;

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
import com.api.servicio.DeudaServicio;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/control/deudas")
public class conDeuda {

	@Autowired
	private DeudaServicio deudaServicio;

	@GetMapping
	public ResponseEntity<List<clientes>> getDeudores(HttpServletRequest request) {
		return ResponseEntity.ok(deudaServicio.getDeudores(request));
	}

	@GetMapping("/{idCliente}/historial")
	public ResponseEntity<List<PagoDeuda>> getHistorial(HttpServletRequest request, @PathVariable Integer idCliente) {
		return ResponseEntity.ok(deudaServicio.getHistorialPagos(request, idCliente));
	}

	@PatchMapping("/pagar")
	public ResponseEntity<clientes> pagar(HttpServletRequest request, @RequestBody PagoDeudaRequest req) {
		return ResponseEntity.ok(deudaServicio.registrarPago(request, req));
	}
}
