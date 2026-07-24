package com.api.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.api.repositorio.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuscripcionService {
	private final SuscripcionRepository suscripcionRepo;
	private final PlanRepository planRepo;
	private final UserRepository userRepo;

	public Suscripcion asignarFree(User user) {
		cancelarSuscripcionActiva(user); // por las dudas, asegura que no quede otra ACTIVA
	    Plan free = planRepo.findById(1)
	            .orElseThrow(() -> new RuntimeException("Plan FREE no encontrado"));

	    Suscripcion suscripcion = new Suscripcion();
	    suscripcion.setUser(user);
	    suscripcion.setPlan(free);
	    suscripcion.setEstado("ACTIVA");
	    suscripcion.setInicio(LocalDateTime.now());
	    // sin vencimiento, sin trial
	    return suscripcionRepo.save(suscripcion);
	}

	public void vencerSuscripcionesExpiradas() {
		List<Suscripcion> activas = suscripcionRepo.findByEstado("ACTIVA");
		LocalDateTime ahora = LocalDateTime.now();

		for (Suscripcion s : activas) {
			if (s.getVencimiento() != null && s.getVencimiento().isBefore(ahora)) {
				s.setEstado("VENCIDA");
				suscripcionRepo.save(s);

				// Darle una nueva suscripción FREE activa
	            asignarFree(s.getUser());
			}
		}
	}

	public Optional<Suscripcion> getSuscripcionActiva(User user) {
		return suscripcionRepo.findByUserAndEstado(user, "ACTIVA");
	}

	public void cancelarSuscripcionActiva(User user) {
		getSuscripcionActiva(user).ifPresent(s -> {
			s.setEstado("CANCELADA");
			suscripcionRepo.save(s);
		});
	}
}
