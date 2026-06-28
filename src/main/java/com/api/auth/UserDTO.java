package com.api.auth;

import java.util.List;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

	private Long id;
	private String nombre;
	private String apellido;
	private Integer edad;
	private String email;
	private String rubro;
	private String rol;
	private Long tenantId;
	private String tenantTipo;
	private String tenantNombre;
	private String nombreFantasia;
	private String razonSocial;
	private String cuit;
	private List<String> permisos;

}
