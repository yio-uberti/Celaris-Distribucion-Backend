package com.api.servicio;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.repositorio.PlanRepository;
import com.api.user.Suscripcion;
import com.api.user.SuscripcionRepository;
import com.api.user.User;
import com.api.user.UserRepository;
import com.google.api.services.androidpublisher.model.SubscriptionPurchaseV2;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BillingService {

	 @Autowired GooglePlayService play;
	    @Autowired UserRepository userRepository;
	    @Autowired PlanRepository planRepository;
	    @Autowired SuscripcionRepository suscripcionRepository;

	    private static final Set<String> ACTIVOS = Set.of(
	        "SUBSCRIPTION_STATE_ACTIVE", "SUBSCRIPTION_STATE_IN_GRACE_PERIOD", "SUBSCRIPTION_STATE_CANCELED");
	        // CANCELED = el usuario canceló pero conserva acceso hasta el vencimiento

	    @Transactional
	    public void sincronizar(String purchaseToken, String productId, String uidSolicitante) throws IOException {
	        SubscriptionPurchaseV2 sp = play.consultar(purchaseToken);

//	        String uid = sp.getExternalAccountIdentifiers() != null
//	            ? sp.getExternalAccountIdentifiers().getObfuscatedExternalAccountId() : null;
//	        log.info("PLAY RESPONSE: {}", sp.toPrettyString());
//	        if (uid == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compra sin usuario asociado");
//	        if (uidSolicitante != null && !uid.equals(uidSolicitante))
//	            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La compra pertenece a otro usuario");
	        
	        log.info("PLAY RESPONSE: {}", sp.toPrettyString());

	        String uidCompra = sp.getExternalAccountIdentifiers() != null
	            ? sp.getExternalAccountIdentifiers().getObfuscatedExternalAccountId() : null;

	        String uid;
	        if (uidSolicitante != null) {
	            if (uidCompra != null && !uidCompra.equals(uidSolicitante))
	                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La compra pertenece a otro usuario");
	            uid = uidSolicitante;
	        } else {
	            Suscripcion existente = suscripcionRepository.findByTokenPago(purchaseToken).orElse(null);
	            if (existente != null) uid = existente.getUser().getFirebaseUid();
	            else if (uidCompra != null) uid = uidCompra;
	            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compra sin usuario asociado");
	        }

	        User user = userRepository.findByFirebaseUid(uid)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

	        var item = sp.getLineItems().get(0);
	        String basePlan = item.getOfferDetails().getBasePlanId();               // "mensual" | "anual"
	        String planNombre = "anual".equals(basePlan) ? "PREMIUM_ANUAL" : "PREMIUM_MENSUAL";
	        LocalDateTime vence = OffsetDateTime.parse(item.getExpiryTime())
	            .atZoneSameInstant(ZoneId.of("America/Argentina/Buenos_Aires")).toLocalDateTime();

	        Suscripcion s = suscripcionRepository.findByTokenPago(purchaseToken).orElse(null);
	        if (s != null && !s.getUser().getId().equals(user.getId()))
	            throw new ResponseStatusException(HttpStatus.CONFLICT, "Compra ya usada por otra cuenta");

	        if (s == null) {
	            suscripcionRepository.cancelarActivas(user.getId());   // reemplaza MP u otra anterior
	            s = new Suscripcion();
	            s.setUser(user);
	            s.setInicio(LocalDateTime.now());
	            s.setMetodoPago("GOOGLE_PLAY");
	            s.setTokenPago(purchaseToken);
	        }
	        s.setPlan(planRepository.findByNombre(planNombre).orElseThrow());
	        s.setVencimiento(vence);
	        s.setProximaRenovacion(vence);
	        s.setEstado(ACTIVOS.contains(sp.getSubscriptionState()) ? "ACTIVA" : "VENCIDA");
	        suscripcionRepository.save(s);

	        if ("ACKNOWLEDGEMENT_STATE_PENDING".equals(sp.getAcknowledgementState())
	                && ACTIVOS.contains(sp.getSubscriptionState()))
	            play.confirmar(productId, purchaseToken);
	    }
}
