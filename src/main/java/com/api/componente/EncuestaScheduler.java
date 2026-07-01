package com.api.componente;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.api.servicio.FcmService;
import com.api.user.User;
import com.api.user.UserRepository;

@Component
public class EncuestaScheduler {

	@Autowired private UserRepository userRepository;
    @Autowired private FcmService fcmService; // tu servicio de Firebase Cloud Messaging

    @Scheduled(cron = "0 0 10 * * *") // todos los días 10am
    public void notificarUsuariosElegibles() {
        LocalDateTime limite = LocalDateTime.now().minusDays(7);
        List<User> elegibles = userRepository.findElegiblesParaEncuesta(limite);

        for (User u : elegibles) {
            if (u.getPushToken() != null) {
                fcmService.enviarNotificacion(
                    u.getPushToken(),
                    "¿Cómo te está yendo con Celaris?",
                    "Queremos saber tu opinión, te toma 1 minuto"
                );
            }
        }
    }
}
