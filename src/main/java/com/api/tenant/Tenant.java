package com.api.tenant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenant", schema = "", catalog = "")
public class Tenant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;

	public Tenant() {
	}

	public Tenant(String nombre) {
		this.nombre = nombre;
	}

	public Long getId() {
		return id;
	}

}
