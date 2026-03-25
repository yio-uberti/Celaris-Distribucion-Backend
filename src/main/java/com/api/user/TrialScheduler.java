package com.api.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.api.repositorio.PlanRepository;

@Component
public class TrialScheduler {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PlanRepository planRepository;

	@Scheduled(fixedRate = 3600000) // cada 1 hora
	public void revocarTrialsVencidos() {
		Plan planFree = planRepository.findById(1).orElse(null);
		if (planFree == null)
			return;

		List<User> usuarios = userRepository.findAll();
		for (User u : usuarios) {
	        if (u.getTrialExpira() != null &&
	            u.getTrialExpira().isBefore(LocalDateTime.now()) &&
	            u.getPlan() != null && u.getPlan().getId() == 2) {

	            u.setPlan(planFree);
	            userRepository.save(u);
	        }
	    }
	}
}
