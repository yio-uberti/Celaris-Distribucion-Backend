package com.api.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.api.entidad.PagoDeuda;

public interface repoPagoDeuda extends CrudRepository<PagoDeuda, Integer>{
	List<PagoDeuda> findAllByClienteIdClienteAndTenantId(Integer idCliente, Long tenantId);
	
	@Modifying
	@Query("DELETE FROM PagoDeuda pd WHERE pd.tenantId = :tenantId")
	void deleteAllByTenantId(@Param("tenantId") Long tenantId);
	
	List<PagoDeuda> findAllByClienteIdClienteAndTenantIdOrderByFechaDesc(Integer idCliente, Long tenantId);
}
