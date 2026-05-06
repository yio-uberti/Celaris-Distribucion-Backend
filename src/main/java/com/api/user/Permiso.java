package com.api.user;

import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permiso", catalog = "", schema = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permiso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private BigInteger id;
	
	@Column(name = "clave")
	private String clave;
	
	@Column(name = "descripcion")
	private String descripcion;
}
