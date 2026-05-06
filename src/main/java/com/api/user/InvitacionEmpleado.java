package com.api.user;

import java.time.LocalDateTime;

import com.api.tenant.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "invitacion_empleado")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitacionEmpleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    private String email;
    private String token;
    private String estado; // PENDIENTE / ACEPTADA / VENCIDA

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "vence_en")
    private LocalDateTime venceEn;
}
