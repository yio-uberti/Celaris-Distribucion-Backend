package com.api.user;

import org.springframework.data.repository.CrudRepository;

public interface EmailsEliminadosRepository extends CrudRepository<Emails_eliminados, Long>{

	boolean existsByEmail(String email);

}
