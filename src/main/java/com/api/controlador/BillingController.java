package com.api.controlador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.servicio.BillingService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/billing")
@Slf4j
public class BillingController {

	 @Autowired BillingService billing;
	    @Value("${PLAY_RTDN_SECRET}") String rtdnSecret;

	    public record VerificarReq(String purchaseToken, String productId) {}

	    @PostMapping("/verificar")
	    public ResponseEntity<?> verificar(@RequestBody VerificarReq req, HttpServletRequest http) throws IOException {
	        String uid = (String) http.getAttribute("firebaseUid");
	        billing.sincronizar(req.purchaseToken(), req.productId(), uid);
	        return ResponseEntity.ok(Map.of("ok", true));
	    }

	    @PostMapping("/rtdn")
	    public ResponseEntity<?> rtdn(@RequestParam String secret, @RequestBody Map<String, Object> body) {
	        if (!rtdnSecret.equals(secret)) return ResponseEntity.status(403).build();
	        try {
	            var msg = (Map<?, ?>) body.get("message");
	            String json = new String(Base64.getDecoder().decode((String) msg.get("data")), StandardCharsets.UTF_8);
	            JsonNode n = new ObjectMapper().readTree(json);
	            JsonNode sn = n.get("subscriptionNotification");
	            if (sn != null)
	                billing.sincronizar(sn.get("purchaseToken").asText(), sn.get("subscriptionId").asText(), null);
	        } catch (Exception e) {
	            log.error("RTDN falló", e);   // respondemos 200 igual para que Pub/Sub no reintente en bucle
	        }
	        return ResponseEntity.ok().build();
	    }
}
