package com.api.controlador;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import com.api.servicio.LimitePlanException;
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
	public ResponseEntity<?> getHistorial(HttpServletRequest request,
	        @RequestParam LocalDate fecha,
	        @RequestParam(required = false) String tipoPago) {
	    List<Ventas> ventas = ventaService.getHistorial(request, fecha, tipoPago);
	    return ResponseEntity.ok(ventas); // el cliente ya viene dentro de cada venta con su saldoDeudor
	}
	
	@GetMapping("/buscar-por-fechas")
	public ResponseEntity<List<Ventas>> buscarPorFechas(
			@RequestParam(value = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
			@RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
			HttpServletRequest request) {
		try {
			List<Ventas> ventas = ventaService.getVentasPorRangoDeFechas(request, fechaInicio, fechaFin);
			return ResponseEntity.ok(ventas);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/hoy")
	public ResponseEntity<List<Ventas>> getVentasHoy(HttpServletRequest request) {
		try {
			LocalDate hoy = LocalDate.now();
			List<Ventas> ventas = ventaService.getVentasPorRangoDeFechas(request, hoy, hoy);
			return ResponseEntity.ok(ventas);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

//	Metodo para registrar una venta 
	@PostMapping
	public ResponseEntity<?> create(HttpServletRequest request, @RequestBody VentaRequest ventaRequest) {
	    try {
	        Ventas creada = ventaService.create(request, ventaRequest);
	        return ResponseEntity.status(201).body(creada);
	    } catch (LimitePlanException e) {
	        return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
	    }
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
	
	
	//Para borrra una lista de ventas en cascada
	// Clase interna para recibir las IDs
	public static class DeleteLoteRequest {
	    private List<Integer> ids;
	    
	    public List<Integer> getIds() {
	        return ids;
	    }
	    
	    public void setIds(List<Integer> ids) {
	        this.ids = ids;
	    }
	}

	// Endpoint DELETE en lote
	@DeleteMapping("/lote")
	public ResponseEntity<?> deleteLote(
	    @RequestBody DeleteLoteRequest request,
	    HttpServletRequest httpRequest) {
	    try {
	        ventaService.deleteLote(request.getIds(), httpRequest);
	        return ResponseEntity.noContent().build();
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(403).body("Error: " + e.getMessage());
	    }
	}

	
//	Metodo para borrar o cancelar una venta individual
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Integer id, HttpServletRequest request) {
	    Long tenantId = ventaService.getTenantIdPublic(request);
	    ventaRepository.findByIdVentaAndTenantId(id, tenantId)
	        .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
	    ventaRepository.deleteById(id);
	    return ResponseEntity.noContent().build();
	}
}
