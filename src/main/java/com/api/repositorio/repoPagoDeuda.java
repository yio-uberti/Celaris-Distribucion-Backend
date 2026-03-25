package com.api.repositorio;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.api.entidad.PagoDeuda;

public interface repoPagoDeuda extends CrudRepository<PagoDeuda, Integer>{
	List<PagoDeuda> findAllByClienteIdClienteAndTenantId(Integer idCliente, Long tenantId);
}
