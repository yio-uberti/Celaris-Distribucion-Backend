package com.api.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SuscripcionRepository extends CrudRepository<Suscripcion, Integer> {

	Optional<Suscripcion> findByUserAndEstado(User user, String estado);
	
	List<Suscripcion> findByEstado(String estado);

	boolean existsByUserAndUsoTrialTrue(User user);

	List<Suscripcion> findByUser(User user);
	
	void deleteByUser(User user);
	
	@Modifying
	@Query("DELETE FROM Suscripcion s WHERE s.user.id = :userId")
	void deleteByUserId(@Param("userId") Long userId);
}
