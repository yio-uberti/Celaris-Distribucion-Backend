package com.api.entidad;

import java.math.BigDecimal;

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
@Table(name = "producto", schema = "", catalog = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class productos {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_prod")
	private Long idProd;
	@Column(name = "nombre_producto")
	private String nombreProducto;
	@Column(name = "precio_actual")
	private BigDecimal precioActual;
	@Column(name ="tenant_id")
	private Long tenantId;
	@Column(name = "tipo_precio")
	private String tipoPrecio;
	
	@ManyToOne
	@JoinColumn(name = "catalogo_id")
	private ProductoCatalogo catalogo;

	@Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
	private Boolean personalizado = false;
	
	@Column(name = "categoria")
	private String categoria;
	
	@Column(name = "marca")
	private String marca;
}
