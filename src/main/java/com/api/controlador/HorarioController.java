package com.api.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.entidad.HorarioEmpleado;
import com.api.servicio.HorarioServicio;
import com.api.servicio.HorarioServicio.HorarioRequest;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/control/horarios")
public class HorarioController {
    
    @Autowired
    private HorarioServicio horarioServicio;
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<HorarioEmpleado>> getHorarios(
        @PathVariable Long empleadoId,
        HttpServletRequest request) {
        return ResponseEntity.ok(horarioServicio.getHorariosEmpleado(empleadoId, request));
    }
    
    @PutMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<HorarioEmpleado>> actualizarHorarios(
        @PathVariable Long empleadoId,
        @RequestBody List<HorarioRequest> horarios,
        HttpServletRequest request) {
        return ResponseEntity.ok(horarioServicio.actualizarHorarios(empleadoId, horarios, request));
    }
    
    @GetMapping("/puede-entrar/{usuarioId}")
    public ResponseEntity<?> puedeEntrar(
        @PathVariable Long usuarioId,
        HttpServletRequest request) {
        
        String firebaseUid = (String) request.getAttribute("firebaseUid");
        User owner = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        User usuario = userRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar que sea del mismo tenant
        if (!usuario.getTenant().getId().equals(owner.getTenant().getId())) {
            return ResponseEntity.status(403).body(
                Map.of("error", "No tienes permisos")
            );
        }
        
        boolean puede = horarioServicio.puedeEntrar(usuarioId);
        return ResponseEntity.ok(puede);
    }
}