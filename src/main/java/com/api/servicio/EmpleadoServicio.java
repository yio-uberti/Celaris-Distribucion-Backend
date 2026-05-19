package com.api.servicio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.api.auth.InvitarEmpleadoRequest;
import com.api.controlador.EmpleadoRequest;
import com.api.tenant.TenantRepository;
import com.api.user.InvitacionEmpleado;
import com.api.user.InvitacionRepository;
import com.api.user.Rol;
import com.api.user.RolRepositorio;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class EmpleadoServicio {

	@Autowired private InvitacionRepository invitacionRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private RolRepositorio rolRepository;
    @Autowired private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;
    
    
    public List<EmpleadoRequest> listarEmpleados(HttpServletRequest request) {
        String firebaseUid = (String) request.getAttribute("firebaseUid");

        User owner = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Long tenantId = owner.getTenant().getId();

        // Todos los users del mismo tenant excepto él mismo
        return userRepository.findByTenantIdAndIdNot(tenantId, owner.getId())
        	    .stream()
        	    .map(u -> new EmpleadoRequest(
        	        u.getId(),
        	        u.getNombre(),
        	        u.getApellido(),
        	        u.getEmail(),
        	        u.getDni(),
        	        u.getRol().getNombre(),
        	        u.getActivo()
        	    ))
        	    .toList();
    }

    public void invitarEmpleado(HttpServletRequest request, InvitarEmpleadoRequest body) {
        String firebaseUid = (String) request.getAttribute("firebaseUid");

        User owner = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Solo OWNER o ADMIN pueden invitar
        String rolNombre = owner.getRol().getNombre();
        if (!rolNombre.equals("OWNER") && !rolNombre.equals("ADMIN")) {
            throw new RuntimeException("Sin permisos para invitar empleados");
        }

        Rol rol = rolRepository.findById(body.getRolId())
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // Generar token único
        String token = UUID.randomUUID().toString();

        InvitacionEmpleado invitacion = new InvitacionEmpleado();
        invitacion.setTenant(owner.getTenant());
        invitacion.setEmail(body.getEmail());
        invitacion.setRol(rol);
        invitacion.setToken(token);
        invitacion.setEstado("PENDIENTE");
        invitacion.setCreadoEn(LocalDateTime.now());
        invitacion.setVenceEn(LocalDateTime.now().plusDays(7));
        invitacionRepository.save(invitacion);

        // Enviar email via Firebase
        enviarEmailInvitacion(body.getEmail(), token, owner.getTenant().getNombreFantasia());
    }

    private void enviarEmailInvitacion(String email, String token, String nombreEmpresa) {
        String link = "celaris-distribuciones://activar?token=" + token; // ← deep link directo a la app

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Te invitaron a unirte a " + nombreEmpresa + " en Celaris");
        mensaje.setText(
            "Hola! Fuiste invitado a unirte a " + nombreEmpresa + ".\n\n" +
            "Tocá este link desde tu celular para activar tu cuenta:\n\n" +
            link + "\n\n" +
            "El link vence en 7 días."
        );
        mailSender.send(mensaje);
    }
}
