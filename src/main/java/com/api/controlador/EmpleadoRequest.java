package com.api.controlador;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoRequest {
	private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private BigInteger dni;
    private String rol;
    private Boolean activo;
}
