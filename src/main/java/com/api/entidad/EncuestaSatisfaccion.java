package com.api.entidad;

import java.time.LocalDateTime;

import com.google.auto.value.AutoValue.Builder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "encuesta_satisfaccion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncuestaSatisfaccion {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String satisfaccion;
    private String facilidadUso;

    @Column(name = "comentario_mejora", columnDefinition = "TEXT")
    private String comentarioMejora;

    @Column(name = "recomendaria_nps")
    private Integer recomendariaNps;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta = LocalDateTime.now();
}
