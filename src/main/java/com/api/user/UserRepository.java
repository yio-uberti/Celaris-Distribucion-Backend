package com.api.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByFirebaseUid(String firebaseUid);

	boolean existsByFirebaseUid(String firebaseUid);
	
	List<User> findAllByPushTokenIsNotNull();

	void deleteAllByTenantId(Long tenantId);

	List<User> findByTenantIdAndIdNot(Long tenantId, Long id);
	
	long countByTenantIdAndRol_NombreNot(Long tenantId, String nombre);

	Optional<User> findByTenantId(Long id);
}
