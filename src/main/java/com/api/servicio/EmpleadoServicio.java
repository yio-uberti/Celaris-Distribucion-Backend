package com.api.servicio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.auth.InvitarEmpleadoRequest;
import com.api.controlador.EmpleadoRequest;
import com.api.user.InvitacionEmpleado;
import com.api.user.InvitacionRepository;
import com.api.user.Rol;
import com.api.user.RolRepositorio;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmpleadoServicio {

	@Autowired private InvitacionRepository invitacionRepository;
    @Autowired private RolRepositorio rolRepository;
    @Autowired private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;
    
    
    private static final Set<String> ROLES_INVITABLES = Set.of("ADMIN", "VENDEDOR", "REPARTIDOR", "EMPLEADO");

    private User gestor(HttpServletRequest request) {
        String uid = (String) request.getAttribute("firebaseUid");
        User u = userRepository.findByFirebaseUid(uid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        String rol = u.getRol().getNombre();
        if (!"OWNER".equals(rol) && !"ADMIN".equals(rol))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permisos");
        return u;
    }

    
    public List<EmpleadoRequest> listarEmpleados(HttpServletRequest request) {
    	User yo = gestor(request);
        boolean soyOwner = "OWNER".equals(yo.getRol().getNombre());
        Long tenantId = yo.getTenant().getId();

        // Todos los users del mismo tenant excepto él mismo
        return userRepository.findByTenantIdAndIdNot(tenantId, yo.getId())
        	    .stream().filter(u -> soyOwner || !"OWNER".equals(u.getRol().getNombre())) 
        	    .map(u -> {
        	        // Buscar la invitación de este empleado para obtener creadoEn
        	    	LocalDateTime creadoEn = invitacionRepository
        	    		    .findByEmailAndTenantIdOrderByCreadoEnDesc(u.getEmail(), tenantId)
        	    		    .stream()
        	    		    .findFirst()
        	    		    .map(InvitacionEmpleado::getCreadoEn)
        	    		    .orElse(null);

        	        return new EmpleadoRequest(
        	            u.getId(),
        	            u.getNombre(),
        	            u.getApellido(),
        	            u.getEmail(),
        	            u.getDni(),
        	            u.getRol().getNombre(),
        	            u.getActivo(),
        	            creadoEn  // ← nuevo
        	        );
        	    })
        	    .toList();
    }

    public void invitarEmpleado(HttpServletRequest request, InvitarEmpleadoRequest body) {
        User owner = gestor(request);
        Long tenantId = owner.getTenant().getId();

        String email = body.getEmail() == null ? "" : body.getEmail().trim().toLowerCase();
        if (!email.matches("^\\S+@\\S+\\.\\S+$"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email inválido");

        Rol rol = rolRepository.findById(body.getRolId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"));
        String nombreRol = rol.getNombre();
        if (!ROLES_INVITABLES.contains(nombreRol))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
        if ("ADMIN".equals(nombreRol) && !"OWNER".equals(owner.getRol().getNombre()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño puede invitar administradores");

        // Mismo chequeo de plan/cupo que usa /auth/puede-agregar-empleado
        // if (!puedeAgregarEmpleado(tenantId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Alcanzaste el límite de empleados");

        if (userRepository.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese email ya tiene una cuenta");
        if (invitacionRepository.existsByEmailAndTenantIdAndEstado(email, tenantId, "PENDIENTE"))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hay una invitación pendiente para ese email");

        String token = UUID.randomUUID().toString();
        InvitacionEmpleado inv = new InvitacionEmpleado();
        inv.setTenant(owner.getTenant());
        inv.setEmail(email);
        inv.setRol(rol);
        inv.setToken(token);
        inv.setEstado("PENDIENTE");
        inv.setCreadoEn(LocalDateTime.now());
        inv.setVenceEn(LocalDateTime.now().plusDays(7));
        inv = invitacionRepository.save(inv);

        try {
            enviarEmailInvitacion(email, token, owner.getTenant().getNombreFantasia());
        } catch (Exception e) {
        	log.error("Fallo al enviar email de invitación a {}", email, e); 
            invitacionRepository.delete(inv);   // que no quede una invitación que nunca llegó
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo enviar el email. Intentá de nuevo.");
        }
    }

    private void enviarEmailInvitacion(String email, String token, String nombreEmpresa) {
        String link = "https://celaris-distribucion-backend.onrender.com/Api-Backend/activar?token=" + token;
        
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
    
 // En EmpleadoServicio
    public void revocarEmpleado(HttpServletRequest request, Long empleadoId) {
        User owner = gestor(request);
        User empleado = userRepository.findById(empleadoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        if (!empleado.getTenant().getId().equals(owner.getTenant().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permisos sobre este empleado");
        if (empleado.getId().equals(owner.getId()) || "OWNER".equals(empleado.getRol().getNombre()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede revocar al dueño");
        if ("ADMIN".equals(empleado.getRol().getNombre()) && !"OWNER".equals(owner.getRol().getNombre()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño puede revocar administradores");

        empleado.setActivo(false);
        userRepository.save(empleado);
    }
}
