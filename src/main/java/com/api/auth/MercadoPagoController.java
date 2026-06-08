package com.api.auth;

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
import com.api.user.Suscripcion;
import com.api.user.SuscripcionRepository;
import com.api.user.User;
import com.api.user.UserRepository;
import com.mercadopago.client.preapproval.PreApprovalAutoRecurringCreateRequest;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.client.preapproval.PreapprovalCreateRequest;
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

	@PostMapping("/crear-pago")
	public ResponseEntity<?> crearPago(@RequestBody PagoMp req, HttpServletRequest httpRequest) {
	    String firebaseUid = (String) httpRequest.getAttribute("firebaseUid");

	    try {
	        boolean esAnual = req.getPlan().equals("PREMIUM_ANUAL");

	        PreapprovalCreateRequest preapprovalRequest = PreapprovalCreateRequest.builder()
	            .reason(esAnual ? "Celaris - Plan Premium Anual" : "Celaris - Plan Premium Mensual")
	            .externalReference(firebaseUid + "|" + req.getPlan())
	            .autoRecurring(
	                PreApprovalAutoRecurringCreateRequest.builder()
	                    .frequency(esAnual ? 12 : 1)          // cada cuántos períodos
	                    .frequencyType(esAnual ? "months" : "months")
	                    .transactionAmount(req.getMonto())     // lo que vos querés cobrar
	                    .currencyId("ARS")
	                    .build()
	            )
	            .backUrl("https://celaris-distribucion-backend.onrender.com")
//	            .notificationUrl("https://celaris-distribucion-backend.onrender.com/Api-Backend/mercado-pago/webhook")
	            .build();

	        PreapprovalClient client = new PreapprovalClient();
	        Preapproval preapproval = client.create(preapprovalRequest);

	        // El usuario va a esta URL a confirmar la suscripción
	        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
	            put("initPoint", preapproval.getInitPoint());
	        }});

	    } catch (Exception e) {
	        return ResponseEntity.status(500).body("Error: " + e.getMessage());
	    }
	}
	
	
//	public ResponseEntity<?> crearPago(@RequestBody PagoMp req, HttpServletRequest httpRequest) {
//		String firebaseUid = (String) httpRequest.getAttribute("firebaseUid");
//
//		try {
//			PreferenceItemRequest item = PreferenceItemRequest.builder().id(req.getPlan())
//					.title(req.getPlan().equals("PREMIUM_ANUAL") ? "Plan Premium Anual - Celaris"
//							: "Plan Premium Mensual - Celaris")
//					.description(req.getPlan())
//					.quantity(1)
//					.currencyId("ARS")
//					.unitPrice(req.getMonto()).build();
//			
//
//			PreferenceRequest preferenceRequest = PreferenceRequest.builder()
//					.items(List.of(item))
//					.externalReference(firebaseUid + "|" + req.getPlan())
//					.notificationUrl(
//							"https://celaris-distribucion-backend.onrender.com/Api-Backend/mercado-pago/webhook")
//					.build();
//
//			PreferenceClient client = new PreferenceClient();
//			Preference preference = client.create(preferenceRequest);
//			return ResponseEntity.ok(preference.getInitPoint());
//
//		} catch (Exception e) {
//			return ResponseEntity.status(500).body("Error: " + e.getMessage());
//		}
//	}

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
	
	
//	@PostMapping("/webhook")
//	public ResponseEntity<?> webhook(@RequestBody Map<String, Object> body) {
//		System.out.println("🔔 WEBHOOK RECIBIDO: " + body); // ← agregá esto
//		try {
//
//			// Formato nuevo (IPN)
//			String type = (String) body.get("type");
//			// Formato viejo (merchant_order)
//			String topic = (String) body.get("topic");
//
//			String paymentId = null;
//
//			if ("payment".equals(type)) {
//				Map<?, ?> data = (Map<?, ?>) body.get("data");
//				paymentId = (String) data.get("id");
//			} else if ("merchant_order".equals(topic)) {
//				return ResponseEntity.ok().build();
//			}
//
//			if (paymentId == null)
//				return ResponseEntity.ok().build();
//
//			PaymentClient paymentClient = new PaymentClient();
//			Payment payment = paymentClient.get(Long.parseLong(paymentId));
//
//			System.out.println("PAYMENT COMPLETO:");
//			System.out.println("ID: " + payment.getId());
//			System.out.println("STATUS: " + payment.getStatus());
//			System.out.println("DESCRIPTION: " + payment.getDescription());
//			System.out.println("EXTERNAL REF: " + payment.getExternalReference());
//			System.out.println("ORDER ID: " + payment.getOrder());
//			System.out.println("METADATA: " + payment.getMetadata());
//			
//			
//			if ("approved".equals(payment.getStatus())) {
//			    String externalRef = payment.getExternalReference();
//			    String[] parts = externalRef.split("\\|");
//			    String firebaseUid = parts[0];
//			    String planNombre = parts[1];
//
//			    User user = userRepository.findByFirebaseUid(firebaseUid)
//			            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//			    Plan plan = planRepository.findByNombre(planNombre)
//			            .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
//
//			    LocalDateTime vencimiento = planNombre.equals("PREMIUM_ANUAL") 
//			            ? LocalDateTime.now().plusYears(1)
//			            : LocalDateTime.now().plusMonths(1);
//
//			    user.setPlan(plan);
//			    userRepository.save(user);
//
//			    suscripcionRepository.cancelarActivas(user.getId());
//
//			    Suscripcion sus = new Suscripcion();
//			    sus.setUser(user);
//			    sus.setPlan(plan);
//			    sus.setEstado("ACTIVA");
//			    sus.setInicio(LocalDateTime.now());
//			    sus.setVencimiento(vencimiento);
//			    sus.setMetodoPago("MERCADOPAGO");
//			    sus.setTokenPago(String.valueOf(payment.getId()));
//			    suscripcionRepository.save(sus);
//			}
//		} catch (Exception e) {
//			System.out.println("Error en webhook: " + e.getMessage());
//		}
//		return ResponseEntity.ok().build();
//	}
}
