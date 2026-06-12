package com.api.user;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanPricingRepository extends CrudRepository<PlanPricing, Long> {
    
	// Query SIMPLE que funciona
	@Query("SELECT p FROM PlanPricing p " +
	       "WHERE p.planId = :planId " +
	       "AND p.tenantTipo = :tenantTipo " +
	       "AND p.activo = true " +
	       "ORDER BY p.createdAt DESC")
	List<PlanPricing> findByPlanAndTenant(@Param("planId") String planId, 
	                                       @Param("tenantTipo") String tenantTipo);
}
