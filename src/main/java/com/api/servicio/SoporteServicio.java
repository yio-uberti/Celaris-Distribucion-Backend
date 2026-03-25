package com.api.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.api.auth.SoporteRequest;
@Service
public class SoporteServicio {
	@Autowired
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String destinatario;

    public void enviarConsulta(SoporteRequest request) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(destinatario);
        mail.setSubject("Nueva consulta de soporte - " + request.getEmail());
        mail.setText("De: " + request.getEmail() + "\n\n" + request.getMensaje());
        mailSender.send(mail);
    }
}
