package com.api.controlador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequest {
	 private String nombreCliente;
	    private List<DetalleRequest> detalles;
	    private List<PagoRequest> pagos;
	    private LocalDate fechaEntrega;
	    private Boolean esReservada; 
	    private Integer descuento; 

	    @Data
	    @Builder
	    @NoArgsConstructor
	    @AllArgsConstructor
	    public static class DetalleRequest {
	        private Long idProducto;
	        private Integer cantidad;
	        private BigDecimal precioUnitario;
	    }

	    @Data
	    @Builder
	    @NoArgsConstructor
	    @AllArgsConstructor
	    public static class PagoRequest {
	        private Integer idTipoPago;
	        private BigDecimal monto;
	    }
	    
	    @Data
	    @Builder
	    @NoArgsConstructor
	    @AllArgsConstructor
	    public static class ConfirmarEntregaRequest{
	    	private List<PagoRequest> pagos;
	    }
	   
}
