package com.api.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class FcmService {

	private static final Logger logger = LoggerFactory.getLogger(FcmService.class);

    public void enviarNotificacion(String pushToken, String titulo, String cuerpo) {
        if (pushToken == null || pushToken.isBlank()) {
            logger.warn("Push token vacío, no se envía notificación");
            return;
        }

        Message message = Message.builder()
                .setToken(pushToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(titulo)
                                .setBody(cuerpo)
                                .build()
                )
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Notificación enviada correctamente: {}", response);
        } catch (FirebaseMessagingException e) {
            logger.error("Error al enviar notificación push a token {}: {}", pushToken, e.getMessage());
        }
    }
	
}
