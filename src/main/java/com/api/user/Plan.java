package com.api.user;

import java.math.BigDecimal;

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
@Table(name = "plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column
	private String nombre;
	@Column
	private BigDecimal precio;
	@Column(name = "max_productos")
	private Integer maxProductos;
	@Column
	private Boolean whatsapp;
	@Column
	private Boolean contabilidad;
	@Column
	private Boolean notificaciones;
	@Column
	private String soporte;
}
