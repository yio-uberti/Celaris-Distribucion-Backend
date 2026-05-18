package com.api.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.repositorio.PlanRepository;
import com.api.user.Plan;
import com.api.user.Suscripcion;
import com.api.user.SuscripcionRepository;
import com.api.user.SuscripcionService;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/planes")
@RequiredArgsConstructor
public class PlanController {

	@Autowired
	private PlanRepository planRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private final SuscripcionService suscripcionService;
	@Autowired
	private SuscripcionRepository suscripcionRepository;

	@GetMapping
	public ResponseEntity<?> getPlanes() {
		return ResponseEntity.ok(planRepository.findAll());
	}

	@GetMapping("/mi-plan")
	public ResponseEntity<?> getMiPlan(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		User user = userRepository.findByFirebaseUid(uid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// Verificar si la suscripción activa venció
		suscripcionService.getSuscripcionActiva(user).ifPresent(s -> {
			if (s.getVencimiento() != null && s.getVencimiento().isBefore(LocalDateTime.now())) {
				s.setEstado("VENCIDA");
				// el service se encarga de bajar a FREE
				suscripcionService.vencerSuscripcionesExpiradas();
			}
		});

		// Refrescar usuario por si cambió
		user = userRepository.findByFirebaseUid(uid).orElseThrow();

		if (user.getPlan() == null) {
			Plan free = planRepository.findById(1).orElseThrow();
			user.setPlan(free);
			userRepository.save(user);
			 return ResponseEntity.ok(
			            new PlanResponse(
			                free.getNombre(),
			                "ACTIVA",
			                null
			            )
			        );
		}

		 Optional<Suscripcion> suscripcion =
		            suscripcionService.getSuscripcionActiva(user);

		    String estado = suscripcion
		            .map(Suscripcion::getEstado)
		            .orElse("SIN_SUSCRIPCION");

		    return ResponseEntity.ok(
		        new PlanResponse(
		            user.getPlan().getNombre(),
		            estado,
		            null
		        )
		    );
	}

	@PostMapping("/iniciar")
	public ResponseEntity<?> iniciarPlanFree(HttpServletRequest request) {
	    String uid = (String) request.getAttribute("firebaseUid");
	    User user = userRepository.findByFirebaseUid(uid)
	            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	    // Si ya tiene suscripción activa no hacer nada
	    if (suscripcionService.getSuscripcionActiva(user).isPresent()) {
	        return ResponseEntity.ok().build();
	    }

	    suscripcionService.asignarFree(user);
	    return ResponseEntity.ok().build();
	}

	@PostMapping("/cancelar")
	public ResponseEntity<?> cancelarPlan(HttpServletRequest request) {
	    String firebaseUid = (String) request.getAttribute("firebaseUid");
	    
	    User user = userRepository.findByFirebaseUid(firebaseUid)
	        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	    
	    // Volver al plan FREE
	    Plan planFree = planRepository.findByNombre("FREE")
	        .orElseThrow(() -> new RuntimeException("Plan FREE no encontrado"));
	    
	    user.setPlan(planFree);
	    userRepository.save(user);
	    
	    // Cancelar suscripciones activas
	    suscripcionRepository.cancelarActivas(user.getId());
	    
	    return ResponseEntity.ok().build();
	}
}