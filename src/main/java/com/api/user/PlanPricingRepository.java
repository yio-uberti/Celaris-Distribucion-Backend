package com.api.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanPricingRepository extends CrudRepository<PlanPricing, Long> {
    
    // Obtener precio actual (vigente) de un plan
    @Query("SELECT p FROM PlanPricing p " +
           "WHERE p.planId = :planId " +
           "AND p.tenantTipo = :tenantTipo " +
           "AND p.activo = true " +
           "AND (p.vicenteHasta IS NULL OR p.vicenteHasta > NOW()) " +
           "ORDER BY p.createdAt DESC LIMIT 1")
    Optional<PlanPricing> findCurrentPrice(String planId, String tenantTipo);
}
