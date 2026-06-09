package com.api.repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.api.controlador.top5productos;
import com.api.entidad.Ventas;

public interface repoVentas extends CrudRepository<Ventas, Integer> {

	List<Ventas> findAllByTenantId(Long tenantId);

	// Id de la venta y tenant al que pertenece
	Optional<Ventas> findByIdVentaAndTenantId(Integer id, Long tenantId);

	// Id del tenant y su estado
	List<Ventas> findAllByTenantIdAndEstado(Long tenantId, String estado);

	// Metodo para notificaciones en la app
	List<Ventas> findAllByTenantIdAndEstadoAndFechaEntrega(Long tenantId, String estado, LocalDate fecha);

	// Top de los 5 productos del dia
	@Query(value = "SELECT p.nombre_producto, SUM(dv.cantidad) as totalVendida " + "FROM producto p "
			+ "JOIN detalleventa dv ON p.id_prod = dv.id_producto " + "JOIN venta v ON dv.id_venta = v.id_venta "
			+ "WHERE DATE(v.fecha_hora) = :fecha " + "AND v.tenant_id = :tenantId " + "GROUP BY p.nombre_producto "
			+ "ORDER BY totalVendida DESC " + "LIMIT 5", nativeQuery = true)
	List<top5productos> listaTop5(@Param("fecha") LocalDate fecha, @Param("tenantId") Long tenantId);

	// Todas las ventas de un día sin filtro de pago
	@Query(value = "SELECT DISTINCT v.* FROM venta v " + "LEFT JOIN users u ON v.registrado_por = u.id "
			+ "LEFT JOIN rol r ON u.rol_id = r.id " + "WHERE v.tenant_id = :tenantId "
			+ "AND DATE(v.fecha_hora) = :fecha " + "AND v.estado != 'RESERVADA' "
			+ "AND (r.nombre IN ('OWNER','ADMIN','VENDEDOR','EMPLEADO') OR v.registrado_por IS NULL)", nativeQuery = true)
	List<Ventas> findByFecha(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

	// Filtrando también por tipo de pago
	@Query(value = "SELECT DISTINCT v.* FROM venta v " + "JOIN ventatipopago vtp ON v.id_venta = vtp.id_venta "
			+ "JOIN tipopago tp ON vtp.id_tipo_pago = tp.id_tipo_pago "
			+ "LEFT JOIN users u ON v.registrado_por = u.id " + "LEFT JOIN rol r ON u.rol_id = r.id "
			+ "WHERE v.tenant_id = :tenantId " + "AND DATE(v.fecha_hora) = :fecha " + "AND v.estado != 'RESERVADA' "
			+ "AND tp.detalle = :tipoPago "
			+ "AND (r.nombre IN ('OWNER','ADMIN','VENDEDOR','EMPLEADO') OR v.registrado_por IS NULL)", nativeQuery = true)
	List<Ventas> findByFechaAndTipoPago(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha,
			@Param("tipoPago") String tipoPago);

//	Metodo de repositorio para borrar registros del usuario
	@Modifying
	@Query("DELETE FROM Ventas v WHERE v.tenantId = :tenantId")
	void deleteAllByTenantId(Long tenantId);

	@Query("SELECT v FROM Ventas v WHERE v.tenantId = :tenantId AND v.fecha_hora BETWEEN :fechaInicio AND :fechaFin ORDER BY v.fecha_hora DESC")
	List<Ventas> findByTenantIdAndFecha_horaBetween(@Param("tenantId") Long tenantId,
			@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
}
