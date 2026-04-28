package com.api.repositorio;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.api.user.Plan;

public interface PlanRepository extends CrudRepository<Plan, Integer>{

	Optional<Plan> findByNombre(String planNombre);

}
