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
@Table(name = "ventatipopago", schema = "", catalog = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaTipoPago {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "id_venta")
	@JsonBackReference("venta-pagos")
	private Ventas venta;
	
	@ManyToOne
	@JoinColumn(name = "id_tipo_pago")
	private TipoPago tipoPago;
	
	@Column
	private BigDecimal monto;
}
