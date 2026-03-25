package com.api.controlador;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoDeudaRequest {
	private Integer idCliente;
	private BigDecimal monto;
	private Integer idTipoPago;
}
