package com.api.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.auth.InvitarEmpleadoRequest;
import com.api.servicio.EmpleadoServicio;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/control/empleados")
public class EmpleadoController {

	@Autowired private EmpleadoServicio empleadoService;

	@GetMapping("/listar")
	public ResponseEntity<?> listar(HttpServletRequest request) {
	    List<EmpleadoRequest> empleados = empleadoService.listarEmpleados(request);
	    return ResponseEntity.ok(empleados);
	}
	
    @PostMapping("/invitar")
    public ResponseEntity<?> invitar(
            @RequestBody InvitarEmpleadoRequest body,
            HttpServletRequest request) {
        empleadoService.invitarEmpleado(request, body);
        return ResponseEntity.ok("Invitación enviada");
    }
    
 // En el controlador
    @PatchMapping("/{id}/revocar")
    public ResponseEntity<?> revocar(@PathVariable Long id, HttpServletRequest request) {
        empleadoService.revocarEmpleado(request, id);
        return ResponseEntity.ok().build();
    }
}
