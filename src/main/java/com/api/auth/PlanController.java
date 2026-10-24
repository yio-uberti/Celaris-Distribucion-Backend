package com.api.auth;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.repositorio.PlanRepository;
import com.api.user.PlanPricing;
import com.api.user.PlanPricingRepository;
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

	@Autowired
	private PlanPricingRepository planPricingRepository;

	@GetMapping
	public ResponseEntity<?> getPlanes() {
		return ResponseEntity.ok(planRepository.findAll());
	}

	// El FRONTEND llama esto para mostrar precios actuales
	@GetMapping("/precios/{tenantTipo}")
	public ResponseEntity<?> obtenerPrecios(@PathVariable String tenantTipo) {
		List<PlanPricing> precios = ((Collection<PlanPricing>) planPricingRepository.findAll()).stream()
				.filter(p -> p.getTenantTipo().equals(tenantTipo) && p.isActivo()).collect(Collectors.toList());

		return ResponseEntity.ok(precios);
	}

	@GetMapping("/mi-plan")
	@Transactional
	public ResponseEntity<?> getMiPlan(HttpServletRequest request) {
	    String uid = (String) request.getAttribute("firebaseUid");
	    User user = userRepository.findByFirebaseUid(uid)
	            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	    Suscripcion s = suscripcionRepository
	            .findFirstByUser_IdAndEstadoOrderByInicioDesc(user.getId(), "ACTIVA")
	            .orElseGet(() -> suscripcionService.asignarFree(user));

	    // Si venció: marcar, dar FREE y devolver FREE
	    if (s.getVencimiento() != null && s.getVencimiento().isBefore(LocalDateTime.now())) {
	        s.setEstado("VENCIDA");
	        suscripcionRepository.save(s);
	        Suscripcion free = suscripcionService.asignarFree(user);
	        return ResponseEntity.ok(new PlanResponse(free.getPlan().getNombre(), "ACTIVA", null));
	    }

	    return ResponseEntity.ok(new PlanResponse(s.getPlan().getNombre(), s.getEstado(), null));
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

		 // Cancelar suscripciones activas
	    suscripcionRepository.cancelarActivas(user.getId());

	    // Darle una suscripción FREE activa
	    suscripcionService.asignarFree(user);

	    return ResponseEntity.ok().build();
	}
}