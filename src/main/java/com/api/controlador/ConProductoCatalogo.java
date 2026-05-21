package com.api.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.entidad.ProductoCatalogo;
import com.api.repositorio.ProductoCatalogoRepository;

@RestController
@RequestMapping("/control/ProductoCatalogo")
public class ConProductoCatalogo {
	
	@Autowired
	private ProductoCatalogoRepository pcRepository;
	
	@PostMapping
	public ResponseEntity<?> cargarProducto(@RequestBody ProductoCatalogo pc) {
	    // Verificar qué está llegando
	    System.out.println("Nombre: " + pc.getNombre());
	    System.out.println("Rubro: " + pc.getRubro());

	    if (pc.getNombre() == null || pc.getNombre().isEmpty()) {
	        return ResponseEntity.badRequest().body("Nombre vacío");
	    }

	    if (pcRepository.findByNombre(pc.getNombre()).isPresent()) {
	        return ResponseEntity.badRequest().body("El producto ya existe en el catálogo");
	    }

	    ProductoCatalogo pcNuevo = new ProductoCatalogo();
	    pcNuevo.setNombre(pc.getNombre());
	    pcNuevo.setRubro(pc.getRubro());
	    pcNuevo.setUnidad(pc.getUnidad());
	    pcNuevo.setCategoria(pc.getCategoria());

	    pcRepository.save(pcNuevo);
	    return ResponseEntity.ok().build();
	}
}
