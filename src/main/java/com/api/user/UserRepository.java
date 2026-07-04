package com.api.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByFirebaseUid(String firebaseUid);

	boolean existsByFirebaseUid(String firebaseUid);

	List<User> findAllByPushTokenIsNotNull();

	void deleteAllByTenantId(Long tenantId);

	List<User> findByTenantIdAndIdNot(Long tenantId, Long id);

	long countByTenantIdAndRol_NombreNot(Long tenantId, String nombre);

	List<User> findByTenantId(Long tenantId);

	@Query("SELECT u FROM User u WHERE u.encuestaCompletada = false " + "AND u.fechaRegistro <= :limite")
	List<User> findElegiblesParaEncuesta(@Param("limite") LocalDateTime limite);
	
	Optional<User> findByTenant_IdAndRol_Id(Long tenantId, Integer rolId);
}
