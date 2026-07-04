package com.api.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface SuscripcionRepository extends CrudRepository<Suscripcion, Integer> {

	Optional<Suscripcion> findByUserAndEstado(User user, String estado);
	
	List<Suscripcion> findByEstado(String estado);

	List<Suscripcion> findByUser(User user);
	
	void deleteByUser(User user);
	
	@Modifying
	@Query("DELETE FROM Suscripcion s WHERE s.user.id = :userId")
	void deleteByUserId(@Param("userId") Long userId);
	
	@Modifying
	@Transactional
	@Query("UPDATE Suscripcion s SET s.estado = 'CANCELADA' WHERE s.user.id = :userId AND s.estado = 'ACTIVA'")
	void cancelarActivas(@Param("userId") Long userId);
    
    // Obtener suscripciones que vencen hoy (para cobrar)
    @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'ACTIVA' AND s.proximaRenovacion <= :ahora")
    List<Suscripcion> findByEstadoAndProximaRenovacionBeforeOrEqual(@Param("estado") String estado, @Param("ahora") LocalDateTime ahora);
    
    // O una versión más explícita:
    @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'ACTIVA' AND s.proximaRenovacion IS NOT NULL AND s.proximaRenovacion <= :ahora")
    List<Suscripcion> findSuscripcionesParaRenovar(@Param("ahora") LocalDateTime ahora);
    
 // SuscripcionRepository
    Optional<Suscripcion> findFirstByUser_IdAndEstadoOrderByInicioDesc(Long userId, String estado);
}
