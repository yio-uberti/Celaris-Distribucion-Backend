package com.api.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.repositorio.repoClientes;
import com.api.repositorio.repoProducto;
import com.api.repositorio.repoVentas;
import com.api.tenant.TenantRepository;
import com.api.user.SuscripcionRepository;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private repoVentas ventaRepository;
	@Autowired
	private repoClientes clienteRepository;
	@Autowired
	private repoProducto productoRepository;
	@Autowired
	private TenantRepository tenantRepository;
	@Autowired
	private SuscripcionRepository suscripcionRepository;

//	Metodo para traer datos del usuario
	@GetMapping("/me")
	public ResponseEntity<?> me(HttpServletRequest request) throws RuntimeException {
		String firebaseUid = (String) request.getAttribute("firebaseUid");
		User user = userRepository.findByFirebaseUid(firebaseUid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		return ResponseEntity.ok(user);
	}

//	Metodo de login o inicio sesion
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(HttpServletRequest request) {
		boolean isNewUser = authService.registerIfNotExists(request);
		return ResponseEntity.ok(Map.of("isNewUser", isNewUser));
	}

//	Metodo para registrar 
	@PostMapping("/register")
	public void register(HttpServletRequest request) {
		authService.registerIfNotExists(request);
	}

//	Metodo para datos personales del usuario
	@PostMapping("/formulario")
	public ResponseEntity<?> formulario(@RequestBody FormularioRequest request, HttpServletRequest httpRequest) {
		authService.completarFormulario(httpRequest, request);
		return ResponseEntity.ok().build();
	}

//	Metodo para actualizar datos personales
	@PutMapping("/me")
	public ResponseEntity<?> updateMe(@RequestBody FormularioRequest request, HttpServletRequest httpRequest) {
		authService.completarFormulario(httpRequest, request);
		return ResponseEntity.ok().build();
	}

//	Metodo para notificaciones, aun debo investigar
	@PostMapping("/push-token")
	public ResponseEntity<?> savePushToken(@RequestBody Map<String, String> body, HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		User user = userRepository.findByFirebaseUid(uid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		user.setPushToken(body.get("pushToken"));
		userRepository.save(user);
		return ResponseEntity.ok().build();
	}

	@Transactional
	@DeleteMapping("/borrar-usuario")
	public ResponseEntity<?> borrar(HttpServletRequest request) {
		String firebaseUid = (String) request.getAttribute("firebaseUid");

		User user = userRepository.findByFirebaseUid(firebaseUid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		Long tenantId = user.getTenantId();
		suscripcionRepository.deleteByUser(user);
		ventaRepository.deleteAllByTenantId(tenantId);
		clienteRepository.deleteAllByTenantId(tenantId);
		productoRepository.deleteAllByTenantId(tenantId);
		userRepository.delete(user);
		tenantRepository.deleteById(tenantId);

		return null;
	}

	@DeleteMapping("/auth/cleanup")
	public ResponseEntity<?> cleanup(HttpServletRequest request) {
		// Solo llega acá si el token Firebase es válido
		// Si el UID no existe en BD → no hay nada que limpiar
		// Si existe → eliminarlo (caso donde se re-registró en Firebase)
		String uid = (String) request.getAttribute("firebaseUid");
		userRepository.findByFirebaseUid(uid).ifPresent(user -> {
			tenantRepository.deleteById(user.getTenantId());
			userRepository.delete(user);
		});
		return ResponseEntity.ok().build();
	}
}
