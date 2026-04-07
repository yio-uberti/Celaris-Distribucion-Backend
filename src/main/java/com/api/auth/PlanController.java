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
import com.api.user.Suscripcion;
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
			return ResponseEntity.ok(free);
		}

		return ResponseEntity.ok(user.getPlan());
	}

	@PostMapping("/trial")
	public ResponseEntity<?> asignarTrial(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		User user = userRepository.findByFirebaseUid(uid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// La fuente de verdad ahora es la tabla suscripcion
		if (suscripcionService.yaUsoTrial(user)) {
			return ResponseEntity.badRequest().body("Ya usaste tu prueba gratuita");
		}

		// Guardar en suscripcion
		Suscripcion trial = suscripcionService.asignarTrial(user);

		// Mantener user sincronizado (campo de conveniencia)
		user.setPlan(trial.getPlan());
		user.setTrialExpira(trial.getVencimiento());
		userRepository.save(user);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/cancelar")
	public ResponseEntity<?> cancelarPlan(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		User user = userRepository.findByFirebaseUid(uid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// Marcar suscripción activa como cancelada
		suscripcionService.cancelarSuscripcionActiva(user);
		
		Plan free = planRepository.findById(1).orElseThrow();
		user.setPlan(free);
		user.setTrialExpira(null);
		userRepository.save(user);

		return ResponseEntity.ok().build();
	}
}