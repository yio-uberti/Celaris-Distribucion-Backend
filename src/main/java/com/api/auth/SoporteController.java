package com.api.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.servicio.SoporteServicio;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/soporte")
public class SoporteController {
	@Autowired
	private SoporteServicio soporteService;

	@PostMapping
	public ResponseEntity<?> enviar(HttpServletRequest httpRequest, @RequestBody SoporteRequest request) {
		soporteService.enviarConsulta(request);
		return ResponseEntity.ok().build();
	}
}
