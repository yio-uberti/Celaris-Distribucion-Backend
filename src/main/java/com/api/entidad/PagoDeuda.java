package com.api.entidad;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "pago_deuda")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class PagoDeuda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private clientes cliente;

    private BigDecimal monto;

    @ManyToOne
    @JoinColumn(name = "id_tipo_pago")
    private TipoPago tipoPago;

    private LocalDateTime fecha;
    @Column(name = "tenant_id")
    private Long tenantId;
}
