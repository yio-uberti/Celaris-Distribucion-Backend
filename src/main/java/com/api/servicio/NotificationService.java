package com.api.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.api.entidad.Ventas;
import com.api.entidad.clientes;
import com.api.repositorio.repoClientes;
import com.api.repositorio.repoVentas;
import com.api.user.User;
import com.api.user.UserRepository;

@Service
public class NotificationService {

	@Autowired
	private repoVentas ventaRepository;
	@Autowired
	private repoClientes clienteRepository;
	@Autowired
	private UserRepository userRepository;

	// Llama a la Expo Push API
	private void enviarNotificacion(String pushToken, String titulo, String cuerpo) {
		try {
			RestTemplate rest = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, String> body = new HashMap<>();
			body.put("to", pushToken);
			body.put("title", titulo);
			body.put("body", cuerpo);
			body.put("sound", "default");

			rest.postForObject("https://exp.host/--/api/v2/push/send", new HttpEntity<>(body, headers), String.class);
		} catch (Exception e) {
			System.out.println("❌ Error enviando notificación: " + e.getMessage());
		}
	}

	// Reservas pendientes para hoy
	@Scheduled(cron = "0 0 8 * * *") // todos los días a las 8am
	public void notificarReservasHoy() {
		List<User> usuarios = userRepository.findAllByPushTokenIsNotNull();
		LocalDate hoy = LocalDate.now();

		for (User user : usuarios) {
			List<Ventas> reservas = ventaRepository.findAllByTenantIdAndEstadoAndFechaEntrega(user.getTenant().getId(),
					"RESERVADA", hoy);
			if (!reservas.isEmpty()) {
				enviarNotificacion(user.getPushToken(), "📦 Entregas para hoy",
						"Tenés " + reservas.size() + " pedido(s) para entregar hoy");
			}
		}
	}

	// Deudores pendientes — frecuencia configurable
	@Scheduled(cron = "0 0 9 * * *")
	public void notificarDeudores() {
		List<User> usuarios = userRepository.findAllByPushTokenIsNotNull();
		String hoy = LocalDate.now().getDayOfWeek().toString(); // MONDAY, TUESDAY...
	    int diaMes = LocalDate.now().getDayOfMonth();
		
		for (User user : usuarios) {
			 boolean debeNotificar = switch (user.getFrecuenciaDeudores()) {
	            case "DIARIA" -> true;
	            case "SEMANAL" -> hoy.equals("MONDAY");
	            case "QUINCENAL" -> diaMes == 1 || diaMes == 15;
	            default -> false;
	        };

	        if (!debeNotificar) continue;
	        //Busca deudores para notificar al proveedor
			List<clientes> deudores = (List<clientes>) clienteRepository
					.findAllByTenantIdAndSaldoDeudorGreaterThan(user.getTenant().getId(), BigDecimal.ZERO);
			if (!deudores.isEmpty()) {
				BigDecimal totalDeuda = deudores.stream().map(clientes::getSaldoDeudor).reduce(BigDecimal.ZERO,
						BigDecimal::add);
				enviarNotificacion(user.getPushToken(), "💰 Deudores pendientes",
						deudores.size() + " cliente(s) deben un total de $" + totalDeuda);
			}
		}
	}
}