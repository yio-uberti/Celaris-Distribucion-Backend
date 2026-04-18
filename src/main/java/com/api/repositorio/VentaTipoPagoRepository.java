package com.api.repositorio;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.api.entidad.VentaTipoPago;

public interface VentaTipoPagoRepository extends CrudRepository<VentaTipoPago, Integer>{
	@Modifying
	@Query("DELETE FROM VentaTipoPago vtp WHERE vtp.venta.tenantId = :tenantId")
	void deleteAllByTenantId(@Param("tenantId") Long tenantId);
}
