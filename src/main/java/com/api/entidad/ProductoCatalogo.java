package com.api.entidad;

import java.math.BigInteger;

import com.api.user.Rubro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto_catalogo", schema = "", catalog = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCatalogo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private BigInteger id;
	
	@Column
	private String nombre;
	
	@ManyToOne
    @JoinColumn(name = "rubro_id", nullable = false)
    private Rubro rubro;

	@Column
	private String unidad;

	@Column
	private String categoria;
	
	@Column(name = "tipo_precio")
	private String tipoPrecio;
}
