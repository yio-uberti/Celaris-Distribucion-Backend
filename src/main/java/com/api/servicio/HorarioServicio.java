package com.api.servicio;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    
    private User gestor(HttpServletRequest request) {
        String uid = (String) request.getAttribute("firebaseUid");
        User u = userRepository.findByFirebaseUid(uid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        String rol = u.getRol().getNombre();
        if (!"OWNER".equals(rol) && !"ADMIN".equals(rol))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permisos");
        return u;
    }

    private User empleadoDelTenant(Long empleadoId, User gestor) {
        User e = userRepository.findById(empleadoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        if (!e.getTenant().getId().equals(gestor.getTenant().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permisos sobre este empleado");
        return e;
    }

    public List<HorarioEmpleado> getHorariosEmpleado(Long empleadoId, HttpServletRequest request) {
        User g = gestor(request);
        empleadoDelTenant(empleadoId, g);
        return horarioRepository.findByUserId(empleadoId);
    }

    @Transactional
    public List<HorarioEmpleado> actualizarHorarios(Long empleadoId, List<HorarioRequest> horarios, HttpServletRequest request) {
        User g = gestor(request);
        User empleado = empleadoDelTenant(empleadoId, g);

        String rolEmp = empleado.getRol().getNombre();
        if ("OWNER".equals(rolEmp) || empleado.getId().equals(g.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede restringir el horario del dueño ni el propio");
        if ("ADMIN".equals(rolEmp) && !"OWNER".equals(g.getRol().getNombre()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño puede editar horarios de administradores");

        if (horarios == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horarios inválidos");

        Set<String> diasValidos = Arrays.stream(DayOfWeek.values()).map(Enum::name).collect(Collectors.toSet());
        Set<String> vistos = new HashSet<>();
        List<HorarioEmpleado> nuevos = new ArrayList<>();

        for (HorarioRequest h : horarios) {
            if (!h.isActivo()) continue;
            if (h.getDiaSemana() == null || !diasValidos.contains(h.getDiaSemana()) || !vistos.add(h.getDiaSemana()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Día inválido o repetido");
            LocalTime ini, fin;
            try {
                ini = LocalTime.parse(h.getHoraInicio());
                fin = LocalTime.parse(h.getHoraFin());
            } catch (DateTimeParseException | NullPointerException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hora inválida (usá HH:mm)");
            }
            if (!ini.isBefore(fin))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de fin debe ser posterior a la de inicio");
            nuevos.add(HorarioEmpleado.builder().user(empleado).diaSemana(h.getDiaSemana())
                    .horaInicio(ini).horaFin(fin).activo(true).build());
        }

        horarioRepository.deleteByUserId(empleadoId);
        entityManager.flush();
        return (List<HorarioEmpleado>) horarioRepository.saveAll(nuevos);
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
    
    public boolean puedeEntrarAutorizado(Long usuarioId, HttpServletRequest request) {
        String uid = (String) request.getAttribute("firebaseUid");
        User yo = userRepository.findByFirebaseUid(uid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        if (!yo.getId().equals(usuarioId)) empleadoDelTenant(usuarioId, gestor(request));
        return puedeEntrar(usuarioId);
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