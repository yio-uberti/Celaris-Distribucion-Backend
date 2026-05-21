package com.api.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.api.entidad.productos;

public interface repoProducto extends CrudRepository<productos, Long> {
	List<productos> findAllByTenantId(Long tenantId);

	Optional<productos> findByIdProdAndTenantId(Long id, Long tenantId);

	Optional<productos> findByNombreProductoAndTenantId(String nombre, Long tenantId);

	@Modifying
	void deleteByNombreProductoAndTenantId(String nombre, Long tenantId);
	
	@Modifying
	@Query("DELETE FROM productos pd WHERE pd.tenantId = :tenantId")
	void deleteAllByTenantId(Long tenantId);
	
	long countByTenantIdAndPrecioActualIsNull(Long tenantId);
}
