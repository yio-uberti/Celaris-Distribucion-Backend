package com.api.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.api.entidad.EncuestaSatisfaccion;

public interface EncuestaSatisfaccionRepository extends JpaRepository<EncuestaSatisfaccion, Long> {

	 // Para evitar respuestas duplicadas y/o consultar la última respuesta de un usuario
    Optional<EncuestaSatisfaccion> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    // Útil si más adelante querés sacar un promedio de NPS general
    @Query("SELECT AVG(e.recomendariaNps) FROM EncuestaSatisfaccion e")
    Double calcularPromedioNps();

    List<EncuestaSatisfaccion> findAllByOrderByFechaRespuestaDesc();
}
