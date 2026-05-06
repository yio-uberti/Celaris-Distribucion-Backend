package com.api.auth;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivarInvitacionRequest {
	private String token;
	private String nombre;
	private String apellido;
	private BigInteger dni;
}
