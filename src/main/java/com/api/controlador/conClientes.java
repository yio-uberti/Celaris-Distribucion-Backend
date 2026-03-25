package com.api.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.entidad.clientes;
import com.api.servicio.ClienteServicio;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/control/cliente")
public class conClientes {

	 @Autowired
	 private ClienteServicio clienteService;

	    @GetMapping
	    public ResponseEntity<List<clientes>> getAll(HttpServletRequest request) {
	        return ResponseEntity.ok(clienteService.getAll(request));
	    }
}
