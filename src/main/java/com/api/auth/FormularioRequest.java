package com.api.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormularioRequest {

	private String nombre;
	private Integer edad;
	private String rubro;
	private String frecuenciaDeudores;
}
