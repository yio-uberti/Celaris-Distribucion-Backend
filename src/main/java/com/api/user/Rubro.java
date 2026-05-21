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
@Table(name = "rubro", schema = "", catalog = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rubro {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private BigInteger id;
	
	@Column
	private String nombre;
}
