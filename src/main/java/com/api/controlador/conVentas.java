package com.api.controlador;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.entidad.Ventas;
import com.api.repositorio.repoVentas;
import com.api.servicio.VentaServicio;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/control/ventas")
public class conVentas {

	@Autowired
	private VentaServicio ventaService;
	@Autowired
	private repoVentas ventaRepository;

	@GetMapping
	public ResponseEntity<List<Ventas>> getAll(HttpServletRequest request) {
		return ResponseEntity.ok(ventaService.getAll(request));
	}

	// Metodo que muestra ventas reservadas
	@GetMapping("/reservadas")
	public ResponseEntity<List<Ventas>> getReservadas(HttpServletRequest request) {
		return ResponseEntity.ok(ventaService.getReservadas(request));
	}

	// Metodo para mostrar top 5 productos dle dia
	@GetMapping("/top-productos")
	public ResponseEntity<List<top5productos>> getTop(HttpServletRequest request) {
		return ResponseEntity.ok(ventaService.getTop5(request));
	}

	// Metodo para solicitar el historial del dia
	@GetMapping("/historial")
	public ResponseEntity<List<Ventas>> getHistorial(HttpServletRequest request, @RequestParam LocalDate fecha,
			@RequestParam(required = false, defaultValue = "todos") String tipoPago) {
		return ResponseEntity.ok(ventaService.getHistorial(request, fecha, tipoPago));
	}

//	Metodo para registrar una venta 
	@PostMapping
	public ResponseEntity<Ventas> create(HttpServletRequest request, @RequestBody VentaRequest ventaRequest) {
		return ResponseEntity.status(201).body(ventaService.create(request, ventaRequest));
	}
	
//	Metodo para editar una venta 
	@PutMapping("/{id}/productos")
	public ResponseEntity<?> editarProductos(
	        @PathVariable Integer id,
	        @RequestBody List<VentaRequest.DetalleRequest> detalles,
	        HttpServletRequest request) {
	    return ResponseEntity.ok(ventaService.editarProductos(id, detalles, request));
	}
	

	@PatchMapping("/{id}/entregar")
	public ResponseEntity<Ventas> entregar(@PathVariable Integer id, HttpServletRequest request,
			@RequestBody VentaRequest.ConfirmarEntregaRequest req) {
		return ResponseEntity.ok(ventaService.confirmarEntrega(id, request, req));
	}
	
//	Metodo para borrar o cancelar una venta
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Integer id, HttpServletRequest request) {
	    Long tenantId = ventaService.getTenantIdPublic(request);
	    ventaRepository.findByIdVentaAndTenantId(id, tenantId)
	        .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
	    ventaRepository.deleteById(id);
	    return ResponseEntity.noContent().build();
	}
}
