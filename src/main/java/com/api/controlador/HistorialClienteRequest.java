package com.api.controlador;

import java.math.BigDecimal;
import java.util.List;

import com.api.entidad.PagoDeuda;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialClienteRequest {
	private String nombreCliente;
    private BigDecimal saldoActual;
    private BigDecimal totalPagado;
    private List<PagoDeuda> pagos;
}
