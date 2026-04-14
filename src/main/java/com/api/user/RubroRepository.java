package com.api.user;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface RubroRepository extends CrudRepository<Rubro, BigInteger>{

	Optional<Rubro> findByNombre(String rubro);

}
