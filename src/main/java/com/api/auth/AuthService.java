package com.api.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.tenant.Tenant;
import com.api.tenant.TenantRepository;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    public boolean registerIfNotExists(HttpServletRequest request) {

        String firebaseUid = (String) request.getAttribute("firebaseUid");
        String email = (String) request.getAttribute("email");

        System.out.println("UID:" + firebaseUid);
        System.out.println("email:" + email);
        
        if (firebaseUid == null || email == null) return false ;

        // Si ya existe en BD → OK, no hacer nada
        if (userRepository.existsByFirebaseUid(firebaseUid)) return false;

        // Si llegó acá, Firebase validó el token (el filtro JWT ya lo hizo)
        // pero no existe en BD → crearlo
        Tenant tenant = tenantRepository.save(
            new Tenant("Proveedor " + email)
        );

        User user = new User();
        user.setFirebaseUid(firebaseUid);
        user.setEmail(email);
        user.setTenantId(tenant.getId());
        userRepository.save(user);
        
        return true;
    }

    public void completarFormulario(HttpServletRequest request, FormularioRequest formulario) {
        String firebaseUid = (String) request.getAttribute("firebaseUid");
        
        if (firebaseUid == null) return;
        
        Optional<User> userOptional = userRepository.findByFirebaseUid(firebaseUid);
        User userData = userOptional.get();
        
        
        if (userData == null) return;
        
        userData.setNombre(formulario.getNombre());
        userData.setEdad(formulario.getEdad());
        userData.setRubro(formulario.getRubro());
        userData.setFrecuenciaDeudores(formulario.getFrecuenciaDeudores());
        
        userRepository.save(userData);
    }
}
