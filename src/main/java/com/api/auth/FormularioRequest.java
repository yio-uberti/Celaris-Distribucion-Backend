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

	private String tipo;
	
	private String nombre;
	private String apellido;
	private Integer edad;
	private String rubro;
	private String frecuenciaDeudores;
	
//	Empresas
	private String nombreFantasia;
    private String razonSocial;
    private String cuit;
    private String telefono;
    
    private String origenReferencia;
}
