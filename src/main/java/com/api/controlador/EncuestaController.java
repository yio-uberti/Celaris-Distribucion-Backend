package com.api.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.dto.EncuestaRequestDTO;
import com.api.servicio.EncuestaService;

@RestController
//@RequestMapping("/api/usuarios/{userId}/encuesta")
@RequestMapping("/{userId}/encuesta")
public class EncuestaController {

    @Autowired private EncuestaService encuestaService;

    @GetMapping("/elegible")
    public ResponseEntity<Boolean> esElegible(@PathVariable Long userId) {
        return ResponseEntity.ok(encuestaService.esElegible(userId));
    }

    @PostMapping
    public ResponseEntity<Void> responder(@PathVariable Long userId,
                                           @RequestBody EncuestaRequestDTO dto) {
        encuestaService.guardarRespuesta(userId, dto);
        return ResponseEntity.ok().build();
    }
}
