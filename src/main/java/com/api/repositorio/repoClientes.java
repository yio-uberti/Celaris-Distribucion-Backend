package com.api.repositorio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.entidad.clientes;

public interface repoClientes extends JpaRepository<clientes, Integer> {

	List<clientes> findAllByTenantId(Long tenantId);
	
	Optional<clientes> findByIdClienteAndTenantId(Integer id, Long tenantId);
	
	//Obtener cliente segun el tenant 
	Optional<clientes> findByNombreClienteAndTenantId(String nombre, Long tenantId);

	//Obtener el saldo deudor del cliente
	List<clientes> findAllByTenantIdAndSaldoDeudorGreaterThan(Long tenantId, BigDecimal saldo);
	
	//Borrar registro relacionado al usuario
	void deleteAllByTenantId(Long tenantId);
}
