package com.api.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipopago", schema = "", catalog = "")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TipoPago {

	@Id
	@Column
	private Integer id_tipo_pago;
	@Column
	private String detalle;
}
 