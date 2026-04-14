package com.api.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.entidad.productos;
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

    @PostMapping
    public ResponseEntity<productos> create(HttpServletRequest request, @RequestBody productos producto) {
        return ResponseEntity.status(201).body(productoService.create(request, producto));
    }

    @PutMapping("/{nombre}")
    public ResponseEntity<productos> update(HttpServletRequest request,
                                           @PathVariable String nombre,
                                           @RequestBody productos producto) {
        return ResponseEntity.ok(productoService.update(request, nombre, producto));
    }
    
    @PutMapping("/actualizacion-masiva")
    public ResponseEntity<ActualizacionResultado> updateMasiva(
            HttpServletRequest request,
            @RequestBody ActualizacionMasivaRequest req) {

    	ActualizacionResultado res = productoService.actualizacionMasiva(request, req.getCambios());
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable String nombre) {
        productoService.delete(request, nombre);
        return ResponseEntity.noContent().build();
    }
}
