package com.api.auth;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.repositorio.PlanRepository;
import com.api.user.Plan;
import com.api.user.PlanPricing;
import com.api.user.PlanPricingRepository;
import com.api.user.Suscripcion;
import com.api.user.SuscripcionRepository;
import com.api.user.User;
import com.api.user.UserRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preapproval.PreApprovalAutoRecurringCreateRequest;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.client.preapproval.PreapprovalCreateRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preapproval.Preapproval;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/mercado-pago")
public class MercadoPagoController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PlanRepository planRepository;
	@Autowired
	private SuscripcionRepository suscripcionRepository;
	@Autowired
	private PlanPricingRepository planPricingRepository;

	@PostMapping("/crear-pago")
	public ResponseEntity<?> crearPago(@RequestBody PagoMp req, HttpServletRequest httpRequest) {
	    String firebaseUid = (String) httpRequest.getAttribute("firebaseUid");

	    try {
	    	String token = System.getenv("MP_ACCESS_TOKEN");
	    	MercadoPagoConfig.setAccessToken(token);
	    	
	        System.out.println("🔍 REQUEST:");
	        System.out.println("   Plan: " + req.getPlan());
	        System.out.println("   Firebase UID: " + firebaseUid);
	        
	     // ✅ NUEVO: Obtener precio desde BD (no del cliente)
	        PlanPricing precioActual = planPricingRepository
	            .findCurrentPrice(req.getPlan(), req.getTenantTipo())  // ← req ahora tiene tenantTipo
	            .orElseThrow(() -> new RuntimeException("Plan no disponible"));
	        
	        BigDecimal montoFinal = precioActual.getMonto();
	        System.out.println("   Monto (desde BD): " + montoFinal);

	        boolean esAnual = req.getPlan().equals("PREMIUM_ANUAL");
	        User user = userRepository.findByFirebaseUid(firebaseUid).orElseThrow();


	        PreapprovalCreateRequest preapprovalRequest = PreapprovalCreateRequest.builder()
	            .reason(esAnual ? "Celaris - Plan Premium Anual" : "Celaris - Plan Premium Mensual")
	            .externalReference(firebaseUid + "|" + req.getPlan())
	            .payerEmail(user.getEmail()) 
	            .autoRecurring(
	                PreApprovalAutoRecurringCreateRequest.builder()
	                    .frequency(esAnual ? 12 : 1)
	                    .frequencyType("months")
	                    .transactionAmount((BigDecimal) montoFinal)
	                    .currencyId("ARS")
	                    .build()
	            )
	            .backUrl("https://celaris-distribucion-backend.onrender.com")
	            .build();

	        System.out.println("📤 Enviando a MercadoPago...");
	        PreapprovalClient client = new PreapprovalClient();
	        Preapproval preapproval = client.create(preapprovalRequest);

	        System.out.println("✅ Preapproval creado: " + preapproval.getId());
	        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
	            put("initPoint", preapproval.getInitPoint());
	        }});

	    } catch (MPApiException e) {
	    	System.out.println("\n❌ ERROR MP API - DETALLES COMPLETOS:");
	        System.out.println("═══════════════════════════════════");
	        System.out.println("Mensaje: " + e.getMessage());
	        
	        // Sacar el status code
	        try {
	            var responseField = e.getClass().getDeclaredField("apiResponse");
	            responseField.setAccessible(true);
	            Object apiResponse = responseField.get(e);
	            
	            System.out.println("\n📋 MPResponse encontrado: " + apiResponse.getClass().getName());
	            
	            // Intentar acceder al status code
	            try {
	                var statusCodeMethod = apiResponse.getClass().getMethod("getStatusCode");
	                int statusCode = (Integer) statusCodeMethod.invoke(apiResponse);
	                System.out.println("Status Code: " + statusCode);
	            } catch (Exception ex) {
	                System.out.println("No se pudo obtener status code");
	            }
	            
	            // Intentar acceder al content/body
	            try {
	                var contentField = apiResponse.getClass().getDeclaredField("content");
	                contentField.setAccessible(true);
	                String content = (String) contentField.get(apiResponse);
	                System.out.println("\n📄 Response Body:");
	                System.out.println(content);
	            } catch (Exception ex) {
	                System.out.println("No se pudo obtener content");
	            }
	            
	            // Intentar acceder a la respuesta como Map
	            try {
	                var bodyField = apiResponse.getClass().getDeclaredField("body");
	                bodyField.setAccessible(true);
	                Object body = bodyField.get(apiResponse);
	                System.out.println("\n📦 Response Body object: " + body);
	            } catch (Exception ex) {
	                System.out.println("No se pudo obtener body");
	            }
	            
	        } catch (Exception ex) {
	            System.out.println("Error extrayendo apiResponse: " + ex.getMessage());
	        }
	        
	        System.out.println("═══════════════════════════════════\n");
	        
	        return ResponseEntity.status(500).body(new java.util.HashMap<String, Object>() {{
	            put("error", "MercadoPago API Error");
	            put("message", e.getMessage());
	        }});
	        
	    } catch (Exception e) {
	        System.out.println("❌ ERROR GENÉRICO:");
	        e.printStackTrace(System.out);
	        return ResponseEntity.status(500).body(new java.util.HashMap<String, Object>() {{
	            put("error", e.getClass().getSimpleName());
	            put("message", e.getMessage());
	        }});
	    }
	}
	

	// ── 2. NUEVO — MP llama esto automáticamente cuando alguien paga ──
	@PostMapping("/webhook")
	public ResponseEntity<?> webhook(@RequestBody Map<String, Object> body) {
	    try {
	        String type = (String) body.get("type");

	        // Suscripción creada/renovada
	        if ("subscription_authorized_payment".equals(type)) {
	            Map<?, ?> data = (Map<?, ?>) body.get("data");
	            String preapprovalId = (String) data.get("id");

	            PreapprovalClient client = new PreapprovalClient();
	            Preapproval preapproval = client.get(preapprovalId);

	            if ("authorized".equals(preapproval.getStatus())) {
	                String externalRef = preapproval.getExternalReference();
	                String[] parts = externalRef.split("\\|");
	                String firebaseUid = parts[0];
	                String planNombre = parts[1];

	                User user = userRepository.findByFirebaseUid(firebaseUid)
	                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	                Plan plan = planRepository.findByNombre(planNombre)
	                    .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

	                LocalDateTime vencimiento = planNombre.equals("PREMIUM_ANUAL")
	                    ? LocalDateTime.now().plusYears(1)
	                    : LocalDateTime.now().plusMonths(1);

	                user.setPlan(plan);
	                userRepository.save(user);

	                suscripcionRepository.cancelarActivas(user.getId());

	                Suscripcion sus = new Suscripcion();
	                sus.setUser(user);
	                sus.setPlan(plan);
	                sus.setEstado("ACTIVA");
	                sus.setInicio(LocalDateTime.now());
	                sus.setVencimiento(vencimiento);
	                sus.setMetodoPago("MERCADOPAGO");
	                sus.setTokenPago(preapprovalId);
	                suscripcionRepository.save(sus);
	            }
	        }
	    } catch (Exception e) {
	        System.out.println("Error en webhook: " + e.getMessage());
	    }
	    return ResponseEntity.ok().build();
	}
	
}
