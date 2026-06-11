package com.api.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.api.entidad.HorarioEmpleado;

@Repository
public interface HorarioEmpleadoRepository extends CrudRepository<HorarioEmpleado, Long> {
    List<HorarioEmpleado> findByUserId(Long userId);
    Optional<HorarioEmpleado> findByUserIdAndDiaSemana(Long userId, String diaSemana);
    void deleteByUserId(Long userId);
}