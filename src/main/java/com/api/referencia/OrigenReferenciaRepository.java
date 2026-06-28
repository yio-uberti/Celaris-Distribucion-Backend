package com.api.referencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrigenReferenciaRepository extends JpaRepository<OrigenReferencia, Long> {
	Optional<OrigenReferencia> findByUsuarioId(Long usuarioId);
	
	List<OrigenReferencia> findByUsuarioIdIn(List<Long> usuarioIds);
}
