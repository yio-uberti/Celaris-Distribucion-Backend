package com.api.servicio;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.dto.EncuestaRequestDTO;
import com.api.entidad.EncuestaSatisfaccion;
import com.api.repositorio.EncuestaSatisfaccionRepository;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EncuestaService {

	@Autowired private UserRepository userRepository;
    @Autowired private EncuestaSatisfaccionRepository encuestaRepository;

    public boolean esElegible(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(user.getEncuestaCompletada())) return false;

        return user.getFechaRegistro()
            .isBefore(LocalDateTime.now().minusDays(7));
    }

    @Transactional
    public void guardarRespuesta(Long userId, EncuestaRequestDTO dto) {
        EncuestaSatisfaccion encuesta = new EncuestaSatisfaccion();
        encuesta.setUserId(userId);
        encuesta.setSatisfaccion(dto.getSatisfaccion());
        encuesta.setFacilidadUso(dto.getFacilidadUso());
        encuesta.setComentarioMejora(dto.getComentarioMejora());
        encuesta.setRecomendariaNps(dto.getRecomendariaNps());
        encuestaRepository.save(encuesta);

        User user = userRepository.findById(userId).orElseThrow();
        user.setEncuestaCompletada(true);
        userRepository.save(user);
    }
}
