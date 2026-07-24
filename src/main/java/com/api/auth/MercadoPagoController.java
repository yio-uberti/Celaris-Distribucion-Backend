package com.api.auth;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

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

	// ── 1. CREAR PAGO (PREFERENCE) ──
	@PostMapping("/crear-pago")
	public ResponseEntity<?> crearPago(@RequestBody PagoMp req, HttpServletRequest httpRequest) {
		String firebaseUid = (String) httpRequest.getAttribute("firebaseUid");
		String tenantTipo = (String) httpRequest.getAttribute("tenantTipo");

		if (tenantTipo == null || tenantTipo.isEmpty()) {
			tenantTipo = req.getTenantTipo();
		}

		try {
			System.out.println("\n🔵 ══════════════════════════════════════");
			System.out.println("   REQUEST CREAR PAGO (PREFERENCE)");
			System.out.println("   Plan: " + req.getPlan());
			System.out.println("   Firebase UID: " + firebaseUid);
			System.out.println("   Tenant: " + tenantTipo);

			// Obtener precio ACTUAL desde BD
			List<PlanPricing> precios = planPricingRepository.findByPlanAndTenant(req.getPlan(), tenantTipo);

			if (precios.isEmpty()) {
				throw new RuntimeException("Plan no disponible para: " + req.getPlan() + " / " + tenantTipo);
			}

			PlanPricing precioActual = precios.get(0);
			BigDecimal montoFinal = precioActual.getMonto();
			System.out.println("   Monto: $" + montoFinal + " ARS");

			boolean esAnual = req.getPlan().equals("PREMIUM_ANUAL");
			User user = userRepository.findByFirebaseUid(firebaseUid)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + firebaseUid));

			// ✅ PREFERENCE para primer pago (SDK 3.1.0)
			PreferenceItemRequest item = PreferenceItemRequest.builder()
					.title("Celaris - Plan " + (esAnual ? "Premium Anual" : "Premium Mensual")).quantity((int) 1L)
					.unitPrice(montoFinal).currencyId("ARS").build();

			PreferencePayerRequest payer = PreferencePayerRequest.builder().email(user.getEmail()).build();

			PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
					.success("https://celaris-distribucion-backend.onrender.com/success")
					.failure("https://celaris-distribucion-backend.onrender.com/failure")
					.pending("https://celaris-distribucion-backend.onrender.com/pending").build();

			PreferenceRequest preferenceRequest = PreferenceRequest.builder().items(List.of(item))
					.externalReference(firebaseUid + "|" + req.getPlan() + "|" + tenantTipo).payer(payer)
					.backUrls(backUrls).autoReturn("approved").build();

			System.out.println("   📤 Enviando a MercadoPago (PREFERENCE)...");
			PreferenceClient client = new PreferenceClient();
			Preference preference = client.create(preferenceRequest);

			System.out.println("   ✅ PREFERENCE CREADO");
			System.out.println("   ID: " + preference.getId());
			System.out.println("   Init Point: " + preference.getInitPoint());
			System.out.println("🔵 ══════════════════════════════════════\n");

			return ResponseEntity.ok(new HashMap<String, String>() {
				{
					put("initPoint", preference.getInitPoint());
					put("preferenceId", preference.getId());
				}
			});

		} catch (MPApiException e) {
			System.out.println("\n🔴 ══════════════════════════════════════");
			System.out.println("   ERROR MERCADOPAGO API");
			System.out.println("   Message: " + e.getMessage());
			System.out.println("🔴 ══════════════════════════════════════\n");

			return ResponseEntity.status(500).body(Map.of("error", "MercadoPago API Error", "message", e.getMessage()));

		} catch (Exception e) {
			System.out.println("\n🔴 ══════════════════════════════════════");
			System.out.println("   ERROR GENERAL");
			System.out.println("   " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(System.out);
			System.out.println("🔴 ══════════════════════════════════════\n");

			return ResponseEntity.status(500)
					.body(Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage()));
		}
	}

// ── 2. WEBHOOK ──
	@PostMapping("/webhook")
	public ResponseEntity<?> webhook(@RequestBody Map<String, Object> body) {
		try {
			String type = (String) body.get("type");
			String action = (String) body.get("action");
			Map<?, ?> data = (Map<?, ?>) body.get("data");

			System.out.println("\n🔔 WEBHOOK RECIBIDO");
			System.out.println("   Type: " + type);
			System.out.println("   Action: " + action);

			if ("payment".equals(type) && data != null && data.containsKey("id")) {
				// ✅ FIX 1: el id del webhook llega como String o Number, convertir a Long
				Object idObj = data.get("id");
				Long paymentId = idObj instanceof Number ? ((Number) idObj).longValue()
						: Long.parseLong(idObj.toString());

				System.out.println("   Payment ID: " + paymentId);
				procesarPago(paymentId);
			}

			return ResponseEntity.ok().build();

		} catch (Exception e) {
			System.out.println("❌ Error webhook: " + e.getMessage());
			e.printStackTrace(System.out);
			return ResponseEntity.ok().build();
		}
	}

// ── 3. PROCESAR PAGO ──
	private void procesarPago(Long paymentId) throws MPApiException, MPException {
		PaymentClient client = new PaymentClient();
		Payment payment = client.get(paymentId);

		System.out.println("   💳 Status: " + payment.getStatus());
		System.out.println("   External Ref: " + payment.getExternalReference());

		if ("approved".equals(payment.getStatus())) {
			String externalRef = payment.getExternalReference();
			String[] parts = externalRef.split("\\|");
			String firebaseUid = parts[0];
			String planNombre = parts[1];
//			String tenantTipo = parts.length > 2 ? parts[2] : "DISTRIBUIDOR";

			User user = userRepository.findByFirebaseUid(firebaseUid)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
			Plan plan = planRepository.findByNombre(planNombre)
					.orElseThrow(() -> new RuntimeException("Plan no encontrado"));

			LocalDateTime vencimiento = planNombre.equals("PREMIUM_ANUAL") ? LocalDateTime.now().plusYears(1)
					: LocalDateTime.now().plusMonths(1);

			// ✅ FIX 2: en SDK 3.1.0 el payment_method_id se obtiene así
			String paymentMethodId = null;
			if (payment.getPaymentMethodId() != null) {
				paymentMethodId = payment.getPaymentMethodId();
				System.out.println("      Payment Method ID: " + paymentMethodId);
			} else {
				System.out.println("      ⚠️  No se pudo obtener payment_method_id");
			}

			userRepository.save(user);

			suscripcionRepository.cancelarActivas(user.getId());

			Suscripcion nuevaSuscripcion = new Suscripcion();
			nuevaSuscripcion.setUser(user);
			nuevaSuscripcion.setPlan(plan);
			nuevaSuscripcion.setEstado("ACTIVA");
			nuevaSuscripcion.setInicio(LocalDateTime.now());
			nuevaSuscripcion.setVencimiento(vencimiento);
			nuevaSuscripcion.setMetodoPago("MERCADOPAGO");
			// ✅ FIX 3: convertir Long a String para setTokenPago
			nuevaSuscripcion.setTokenPago(String.valueOf(paymentId));
			nuevaSuscripcion.setPaymentMethodId(paymentMethodId);
			nuevaSuscripcion.setProximaRenovacion(vencimiento);

			suscripcionRepository.save(nuevaSuscripcion);
			System.out.println("      ✅ SUSCRIPCIÓN ACTIVADA EXITOSAMENTE\n");

		} else {
			System.out.println("   ❌ Pago " + payment.getStatus());
		}
	}

// ── 4. COBRO AUTOMÁTICO ──
	public void cobrarRenovacionesVencidas() throws MPApiException {
		List<Suscripcion> suscripcionesAVencer = suscripcionRepository
				.findSuscripcionesParaRenovar(LocalDateTime.now().plusHours(1));

		for (Suscripcion sus : suscripcionesAVencer) {
			try {
				if (sus.getPaymentMethodId() == null || sus.getPaymentMethodId().isEmpty()) {
					sus.setEstado("PENDIENTE_PAGO");
					suscripcionRepository.save(sus);
					continue;
				}

				List<PlanPricing> precios = planPricingRepository.findByPlanAndTenant(
				        sus.getPlan().getNombre(),
				        sus.getUser().getTenant().getTipo());

				if (precios.isEmpty())
					continue;

				BigDecimal montoActual = precios.get(0).getMonto();

				PaymentClient paymentClient = new PaymentClient();

				// ✅ FIX 4: builder correcto para SDK 3.1.0
				// .amount() NO existe → usar .transactionAmount()
				// .payerEmail() NO existe → usar .payer() con PaymentPayerRequest
				com.mercadopago.client.payment.PaymentPayerRequest payer = com.mercadopago.client.payment.PaymentPayerRequest
						.builder().email(sus.getUser().getEmail()).build();

				com.mercadopago.client.payment.PaymentCreateRequest paymentRequest = com.mercadopago.client.payment.PaymentCreateRequest
						.builder().transactionAmount(montoActual) // ✅ nombre correcto
						.paymentMethodId(sus.getPaymentMethodId()).payer(payer) // ✅ payer anidado
						.description("Renovación - " + sus.getPlan().getNombre())
						.externalReference(
								sus.getUser().getFirebaseUid() + "|" + sus.getPlan().getNombre() + "|RENOVACION")
						.build();

				Payment paymentRenovacion = paymentClient.create(paymentRequest);

				if ("approved".equals(paymentRenovacion.getStatus())) {
					LocalDateTime nuevoVencimiento = sus.getPlan().getNombre().equals("PREMIUM_ANUAL")
							? sus.getVencimiento().plusYears(1)
							: sus.getVencimiento().plusMonths(1);

					sus.setVencimiento(nuevoVencimiento);
					sus.setProximaRenovacion(nuevoVencimiento);
					// ✅ FIX 5: convertir Long a String
					sus.setTokenPago(String.valueOf(paymentRenovacion.getId()));
					sus.setEstado("ACTIVA");
					suscripcionRepository.save(sus);
				} else {
					sus.setEstado("PENDIENTE_PAGO");
					suscripcionRepository.save(sus);
				}

			} catch (Exception e) {
				System.out.println("      ❌ Error renovando: " + e.getMessage());
			}
		}
	}

}