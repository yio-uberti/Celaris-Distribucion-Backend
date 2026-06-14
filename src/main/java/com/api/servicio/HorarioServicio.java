package com.api.servicio;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.entidad.HorarioEmpleado;
import com.api.repositorio.HorarioEmpleadoRepository;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class HorarioServicio {
    @Autowired
    private HorarioEmpleadoRepository horarioRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private jakarta.persistence.EntityManager entityManager;
    
    // GET - obtener horarios de un empleado
    public List<HorarioEmpleado> getHorariosEmpleado(Long empleadoId, HttpServletRequest request) {
        // Validar que el usuario tenga permisos
        String firebaseUid = (String) request.getAttribute("firebaseUid");
        User owner = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar que sea del mismo tenant
        User empleado = userRepository.findById(empleadoId)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        
        if (!empleado.getTenant().getId().equals(owner.getTenant().getId())) {
            throw new RuntimeException("No tienes permisos");
        }
        
        return horarioRepository.findByUserId(empleadoId);
    }
    
    // PUT - actualizar/crear horarios de un empleado
    @Transactional
    public List<HorarioEmpleado> actualizarHorarios(Long empleadoId, List<HorarioRequest> horarios, HttpServletRequest request) {
        String firebaseUid = (String) request.getAttribute("firebaseUid");
        User owner = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        User empleado = userRepository.findById(empleadoId)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        
        // Verificar permisos
        if (!empleado.getTenant().getId().equals(owner.getTenant().getId())) {
            throw new RuntimeException("No tienes permisos");
        }
        
        // Eliminar horarios anteriores
        horarioRepository.deleteByUserId(empleadoId);
        entityManager.flush();
        
        // Guardar nuevos horarios
        List<HorarioEmpleado> nuevosHorarios = horarios.stream()
            .filter(h -> h.isActivo()) // Solo guardar los activos
            .map(h -> HorarioEmpleado.builder()
                .user(empleado)
                .diaSemana(h.getDiaSemana())
                .horaInicio(LocalTime.parse(h.getHoraInicio()))
                .horaFin(LocalTime.parse(h.getHoraFin()))
                .activo(true)
                .build())
            .toList();
        
        return (List<HorarioEmpleado>) horarioRepository.saveAll(nuevosHorarios);
    }
    
    // GET - validar si un usuario puede entrar ahora
    public boolean puedeEntrar(Long usuarioId) {
        List<HorarioEmpleado> horarios = horarioRepository.findByUserId(usuarioId);
        
        if (horarios.isEmpty()) {
            return true; // Si no tiene horarios asignados, puede entrar (ej: owner)
        }
        
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now(
            java.time.ZoneId.of("America/Argentina/Buenos_Aires")
        );
        java.time.DayOfWeek diahoy = ahora.getDayOfWeek();
        LocalTime horaAhora = ahora.toLocalTime();
        
        String diaSemanaHoy = diahoy.name();
        
        return horarios.stream()
            .filter(h -> h.getDiaSemana().equals(diaSemanaHoy))
            .anyMatch(h -> !horaAhora.isBefore(h.getHoraInicio()) && 
                          horaAhora.isBefore(h.getHoraFin()));
    }
    
    // DTO
    public static class HorarioRequest {
        private String diaSemana;
        private String horaInicio; // HH:mm
        private String horaFin;    // HH:mm
        private boolean activo;
        
        public String getDiaSemana() { return diaSemana; }
        public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
        public String getHoraInicio() { return horaInicio; }
        public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
        public String getHoraFin() { return horaFin; }
        public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
    }
}