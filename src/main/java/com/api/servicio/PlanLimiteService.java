package com.api.servicio;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.api.user.SuscripcionRepository;
import com.api.user.User;
import com.api.user.UserRepository;
import com.google.auto.value.AutoValue.Builder;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Data
@Builder
public class PlanLimiteService {

    private final UserRepository userRepository;
    private final SuscripcionRepository suscripcionRepository;

    private static final int ID_PLAN_FREE = 1;
    private static final int ID_ROL_OWNER = 1;

    // La suscripción cuelga del owner, no del tenant — en Empresa hay que
    // ir a buscarlo explícitamente, un empleado no tiene su propia fila
    // en Suscripcion y no debería bloquearse si el owner ya paga.
    public boolean tenantEsPremium(Long tenantId) {
        Optional<User> ownerOpt =
            userRepository.findByTenant_IdAndRol_Id(tenantId, ID_ROL_OWNER);

        if (ownerOpt.isEmpty()) {
            return false;
        }

        Long ownerId = ownerOpt.get().getId();

        return suscripcionRepository
            .findFirstByUser_IdAndEstadoOrderByInicioDesc(ownerId, "ACTIVA")
            .map(s ->
                s.getPlan().getId() != ID_PLAN_FREE
                && (s.getVencimiento() == null || s.getVencimiento().isAfter(LocalDateTime.now()))
            )
            .orElse(false);
    }
}
