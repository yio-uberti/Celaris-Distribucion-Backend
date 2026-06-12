package com.api.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface InvitacionRepository extends CrudRepository<InvitacionEmpleado, Long>{
	Optional<InvitacionEmpleado> findByTokenAndEstado(String token, String estado);

	Optional<InvitacionEmpleado> findByEmailAndTenantId(String email, Long tenantId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM InvitacionEmpleado i WHERE i.tenant.id = :tenantId")
	void deleteAllByTenantId(@Param("tenantId") Long tenantId);
}
