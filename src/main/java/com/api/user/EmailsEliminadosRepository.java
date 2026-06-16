package com.api.user;

import org.springframework.data.repository.CrudRepository;

public interface EmailsEliminadosRepository extends CrudRepository<Emails_eliminados, String>{

	boolean existsByEmail(String email);

}
