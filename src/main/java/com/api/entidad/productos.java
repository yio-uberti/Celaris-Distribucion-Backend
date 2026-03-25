package com.api.entidad;

import java.math.BigDecimal;

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
	@Column
	private BigDecimal precio_actual;
	@Column(name ="tenant_id")
	private Long tenantId;
}
