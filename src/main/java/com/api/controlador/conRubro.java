package com.api.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.user.Rubro;
import com.api.user.RubroRepository;


@RestController
@RequestMapping("/control/rubro")
public class conRubro {

	@Autowired
	private RubroRepository rubroRepository;
	
	@GetMapping()
	public ResponseEntity<List<Rubro>> traerRubros() {
		return ResponseEntity.ok((List<Rubro>) rubroRepository.findAll());
	}
	
}
