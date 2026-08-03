package com.api.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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
	
	long countByTenantId(Long tenantId);
	
	// Repository — LIKE con límite, mucho más liviano que traer todo
	@Query(value = "SELECT * FROM producto WHERE tenant_id = :tenantId " +
	        "AND nombre_producto LIKE CONCAT('%', :query, '%') " +
	        "AND (precio_actual > 0 OR tipo_precio = 'PRECIO_VARIABLE') " +
	        "ORDER BY nombre_producto LIMIT 15", nativeQuery = true)
	List<productos> buscarPorNombre(@Param("tenantId") Long tenantId, @Param("query") String query);
	
	
	// Repository -- buscador liviano por nombre, marca y categoria
	@Query(value = "SELECT * FROM producto WHERE tenant_id = :tenantId " +
	        "AND (precio_actual > 0 OR tipo_precio = 'PRECIO_VARIABLE') " +
	        "AND (nombre_producto LIKE CONCAT('%', :query, '%') " +
	        "     OR categoria LIKE CONCAT('%', :query, '%') " +
	        "     OR marca LIKE CONCAT('%', :query, '%')) " +
	        "ORDER BY " +
	        "  CASE WHEN nombre_producto LIKE CONCAT(:query, '%') THEN 0 " +
	        "       WHEN categoria LIKE CONCAT(:query, '%') THEN 1 " +
	        "       WHEN marca LIKE CONCAT(:query, '%') THEN 2 " +
	        "       ELSE 3 END, " +
	        "  nombre_producto " +
	        "LIMIT 15", nativeQuery = true)
	List<productos> buscarPorNombreCategoriaMarca(@Param("tenantId") Long tenantId, @Param("query") String query);
	
	@Query(value = "SELECT + FROM producto WHERE tenant_id = :tenantId " + 
					"ORDER BY " + 
					"CASE WHEN categoria IS NULL OR categoria = '' THEN 1 ELSE 0 END, " +
					"categoria, " + 
					"CASE WHEN marca IS NULL OR marca = '' THEN 1 ELSE 0 END, " + 
					"marca, " + 
					"nombre_producto", nativeQuery = true)
	List<productos> listaCategorizada(@Param("tenantId") Long tenantId);
}
