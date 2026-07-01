package com.api.dto;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EncuestaRequestDTO {

	private String satisfaccion;
    private String facilidadUso;
    private String comentarioMejora;
    private Integer recomendariaNps;
	
}
