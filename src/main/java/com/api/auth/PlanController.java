package com.api.auth;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.repositorio.PlanRepository;
import com.api.user.Plan;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/planes")
public class PlanController {
	@Autowired
	private PlanRepository planRepository;
	@Autowired
	private UserRepository userRepository;

	@GetMapping
	public ResponseEntity<?> getPlanes() {
		return ResponseEntity.ok(planRepository.findAll());
	}

	@GetMapping("/mi-plan")
	public ResponseEntity<?> getMiPlan(HttpServletRequest request) {
	    String uid = (String) request.getAttribute("firebaseUid");
	    User user = userRepository.findByFirebaseUid(uid)
	        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	    
	 // Si el trial venció → bajar a FREE automáticamente
	    if (user.getTrialExpira() != null &&
	        user.getTrialExpira().isBefore(LocalDateTime.now())) {

	        Plan planFree = planRepository.findById(1)
	            .orElseThrow(() -> new RuntimeException("Plan FREE no encontrado"));
	        user.setPlan(planFree);
	        user.setTrialExpira(null); // limpiar trial
	        userRepository.save(user);
	        return ResponseEntity.ok(planFree);
	    }

	    if (user.getPlan() == null) {
	        Plan planFree = planRepository.findById(1)
	            .orElseThrow(() -> new RuntimeException("Plan FREE no encontrado"));
	        user.setPlan(planFree);
	        userRepository.save(user);
	        return ResponseEntity.ok(planFree);
	    }

	    return ResponseEntity.ok(user.getPlan());
	}
	
	// asignar prueba gratuita al usuario de plan gratuito ── 
    @PostMapping("/trial")
    public ResponseEntity<?> asignarTrial(HttpServletRequest request) {
        String uid = (String) request.getAttribute("firebaseUid");
        User user = userRepository.findByFirebaseUid(uid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si ya tiene trial o premium no asignar de nuevo
        if (user.getTrialExpira() != null || 
            (user.getPlan() != null && user.getPlan().getId() == 2)) {
            return ResponseEntity.badRequest().body("Ya usaste tu prueba gratuita");
        }

        // Asignar plan PREMIUM por 15 días
        Plan planPremium = planRepository.findById(2)
            .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        user.setPlan(planPremium);
        user.setTrialExpira(LocalDateTime.now().plusDays(15));
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
    
//    Cancelar plan cuando lo desee el usuario
    @PostMapping("/cancelar")
    public ResponseEntity<?> cancelarPlan(HttpServletRequest request) {
        String uid = (String) request.getAttribute("firebaseUid");
        User user = userRepository.findByFirebaseUid(uid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Plan planFree = planRepository.findById(1)
            .orElseThrow(() -> new RuntimeException("Plan FREE no encontrado"));

        user.setPlan(planFree);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}