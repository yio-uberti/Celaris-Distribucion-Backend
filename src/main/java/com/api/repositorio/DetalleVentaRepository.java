package com.api.repositorio;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.api.entidad.DetalleVenta;

public interface DetalleVentaRepository extends CrudRepository<DetalleVenta, Integer>{
	@Modifying
	@Query("DELETE FROM DetalleVenta dv WHERE dv.venta.tenantId = :tenantId")
	void deleteAllByTenantId(@Param("tenantId") Long tenantId);
}
