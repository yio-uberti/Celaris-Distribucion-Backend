package com.api.repositorio;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.api.entidad.ProductoCatalogo;
import com.api.user.Rubro;

public interface ProductoCatalogoRepository extends CrudRepository<ProductoCatalogo, BigInteger>{

	List<ProductoCatalogo> findByRubro(Rubro rubro);

	// ProductoCatalogoRepository
	Optional<ProductoCatalogo> findByNombre(String nombre);
	Optional<ProductoCatalogo> findByNombreAndRubroNombre(String nombre, String rubroNombre);


}
