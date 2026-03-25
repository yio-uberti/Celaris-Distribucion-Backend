package com.api.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.entidad.TipoPago;
import com.api.repositorio.repoTipoPago;

@RestController
@RequestMapping("/control/tipoPago")
public class conTipoPago {

	@Autowired
	private repoTipoPago tipoPagoRepositoty;

	@GetMapping
	public ResponseEntity<List<TipoPago>> tipoPago() {
		return ResponseEntity.ok((List<TipoPago>) tipoPagoRepositoty.findAll());
	}
}
