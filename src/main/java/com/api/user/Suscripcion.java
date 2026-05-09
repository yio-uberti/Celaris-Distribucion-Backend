package com.api.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suscripcion")
@Data
@NoArgsConstructor
public class Suscripcion {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private String estado = "ACTIVA"; // ACTIVA, VENCIDA, CANCELADA, PENDIENTE

    @Column(nullable = false)
    private LocalDateTime inicio = LocalDateTime.now();

    private LocalDateTime vencimiento;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "token_pago")
    private String tokenPago;
}
