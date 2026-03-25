package com.api.entidad;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
@Table(name = "detalleventa", schema = "", catalog = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private Integer id_detalle;
	
	@ManyToOne
	@JoinColumn(name = "id_venta")
	@JsonBackReference
	private Ventas venta;
	
	@ManyToOne
	@JoinColumn(name = "id_producto")
	private productos producto;
	
	@Column
	private Integer cantidad;
	@Column
	private BigDecimal precio_unitario;
	@Column
	private BigDecimal subtotal;
	
	
}
