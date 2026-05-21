package com.api.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface InvitacionRepository extends CrudRepository<InvitacionEmpleado, Long>{
	Optional<InvitacionEmpleado> findByTokenAndEstado(String token, String estado);

	@Modifying
	@Query("DELETE FROM InvitacionEmpleado i WHERE i.tenantId = :tenantId")
	void deleteAllByTenantId(Long tenantId);
}
