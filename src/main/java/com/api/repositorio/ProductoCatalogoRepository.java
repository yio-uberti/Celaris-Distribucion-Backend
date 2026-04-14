package com.api.repositorio;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.api.entidad.ProductoCatalogo;
import com.api.user.Rubro;

public interface ProductoCatalogoRepository extends CrudRepository<ProductoCatalogo, BigInteger>{

	List<ProductoCatalogo> findByRubro(Rubro rubro);

}
