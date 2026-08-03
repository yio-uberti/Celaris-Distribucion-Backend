package com.api.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.entidad.productos;
import com.api.servicio.LimitePlanException;
import com.api.servicio.ProductoServicio;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/control/productos")
public class conProductos {
	@Autowired 
	private ProductoServicio productoService;

    @GetMapping
    public ResponseEntity<List<productos>> getAll(HttpServletRequest request) {
        return ResponseEntity.ok(productoService.getAll(request));
    }
    
    @GetMapping("/categorizados")
    public ResponseEntity<List<productos>> getCategorizados(HttpServletRequest request) {
        return ResponseEntity.ok(productoService.getAllcategorizados(request));
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarProductos(
            @RequestParam String query,
            HttpServletRequest request) {
        List<productos> productos = productoService.buscarPorNombre(request, query);
        return ResponseEntity.ok(productos);
    }

    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request, @RequestBody productos producto) {
    	try {
            productos creado = productoService.create(request, producto);
            return ResponseEntity.status(201).body(creado);
        } catch (LimitePlanException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{nombre}")
    public ResponseEntity<productos> update(HttpServletRequest request,
                                           @PathVariable String nombre,
                                           @RequestBody productos producto) {
        return ResponseEntity.ok(productoService.update(request, nombre, producto));
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable String nombre) {
        productoService.delete(request, nombre);
        return ResponseEntity.noContent().build();
    }
}
