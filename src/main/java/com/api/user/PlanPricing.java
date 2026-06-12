package com.api.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.google.auto.value.AutoValue.Builder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan_pricing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPricing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String planId;  // PREMIUM_MENSUAL, PREMIUM_ANUAL
    
    @Column(nullable = false)
    private String tenantTipo;  // AUTONOMO, EMPRESA
    
    @Column(nullable = false)
    private BigDecimal monto;
    
    @Column(nullable = false)
    private String moneda = "ARS";
    
    @Column(nullable = false)
    private boolean activo = true;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
