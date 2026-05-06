package com.api.user;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface InvitacionRepository extends CrudRepository<InvitacionEmpleado, Long>{
	Optional<InvitacionEmpleado> findByTokenAndEstado(String token, String estado);
}
