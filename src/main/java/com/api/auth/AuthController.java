package com.api.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.repositorio.DetalleVentaRepository;
import com.api.repositorio.VentaTipoPagoRepository;
import com.api.repositorio.repoClientes;
import com.api.repositorio.repoPagoDeuda;
import com.api.repositorio.repoProducto;
import com.api.repositorio.repoVentas;
import com.api.tenant.Tenant;
import com.api.tenant.TenantRepository;
import com.api.user.EmailsEliminadosRepository;
import com.api.user.Emails_eliminados;
import com.api.user.InvitacionEmpleado;
import com.api.user.InvitacionRepository;
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
	@Autowired
	private DetalleVentaRepository detalleVentaRepository;
	@Autowired
	private VentaTipoPagoRepository ventaTipoPagoRepository;
	@Autowired
	private repoPagoDeuda pagoDeudaRepository;
	@Autowired
	private InvitacionRepository invitacionRepository;
	@Autowired
	private EmailsEliminadosRepository emailsEliminadosRepository; 

//	Metodo para traer datos del usuario
	@GetMapping("/me")
	public ResponseEntity<?> me(HttpServletRequest request) {
	    String firebaseUid = (String) request.getAttribute("firebaseUid");
	    User user = userRepository.findByFirebaseUid(firebaseUid)
	            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	    Tenant tenant = user.getTenant();

	    Map<String, Object> response = new HashMap<>();
	    response.put("id", user.getId());
	    response.put("nombre", user.getNombre());
	    response.put("apellido", user.getApellido());
	    response.put("edad", user.getEdad());
	    response.put("email", user.getEmail());
	    response.put("rubro", user.getRubro());
	    response.put("rol", user.getRol().getNombre());
	    response.put("tenantId", tenant.getId());
	    response.put("tenantTipo", tenant.getTipo());           // AUTONOMO / EMPRESA
	    response.put("tenantNombre", tenant.getNombre());       // nombre del negocio/empresa
	    response.put("permisos", user.getRol().getPermisos()
	            .stream().map(p -> p.getClave()).toList());

	    return ResponseEntity.ok(response);
	}

	@GetMapping("/productos-sin-precio")
	public ResponseEntity<Boolean> tieneProductosSinPrecio(HttpServletRequest request) {
		boolean sinPrecio = authService.tieneProductosSinPrecio(request);
		return ResponseEntity.ok(sinPrecio);
	}
	
	@GetMapping("/invitacion/{token}")
	public ResponseEntity<?> getInvitacion(@PathVariable String token) {
	    InvitacionEmpleado inv = invitacionRepository
	        .findByTokenAndEstado(token, "PENDIENTE")
	        .orElseThrow(() -> new RuntimeException("Inválida"));
	    return ResponseEntity.ok(Map.of("email", inv.getEmail()));
	}

//	Metodo de login o inicio sesion
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(HttpServletRequest request) {
		String email = (String) request.getAttribute("email");

	    // Verificar si el email fue eliminado previamente
	    if (email != null && emailsEliminadosRepository.existsByEmail(email)) {
	        return ResponseEntity.status(403)
	            .body(Map.of("error", "Esta cuenta fue eliminada y no puede volver a registrarse."));
	    }
		
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
	
//	Metodo para que las empresas creen empleados
	@PostMapping("/activar-invitacion")
	public ResponseEntity<?> activarInvitacion(
	        @RequestBody ActivarInvitacionRequest body,
	        HttpServletRequest request) {
	    authService.activarInvitacion(request, body);
	    return ResponseEntity.ok().build();
	}

	@Transactional
	@DeleteMapping("/borrar-usuario")
	public ResponseEntity<?> borrar(HttpServletRequest request) {
		try {
			String firebaseUid = (String) request.getAttribute("firebaseUid");
			String email = (String) request.getAttribute("email"); 
			
			User user = userRepository.findByFirebaseUid(firebaseUid)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			// Guardar email en lista negra ANTES de borrar todo
	        if (email != null) {
	            Emails_eliminados emailEliminado = Emails_eliminados.builder()
	                .email(email)
	                .fechaEliminacion(LocalDateTime.now())
	                .build();
	            emailsEliminadosRepository.save(emailEliminado);
	        }
			
			
			Long tenantId = user.getTenant().getId();
			Long userId = user.getId();

			// ORDEN CRÍTICO: borrar de hijos a padres

			// 1. Borrar detalles de venta (FK a venta)
			detalleVentaRepository.deleteAllByTenantId(tenantId);

			// 2. Borrar venta_tipo_pago (FK a venta)
			ventaTipoPagoRepository.deleteAllByTenantId(tenantId);

			// 3. Borrar ventas (FK a cliente)
			ventaRepository.deleteAllByTenantId(tenantId);

			// 4. Borrar pagos de deuda (FK a cliente)
			pagoDeudaRepository.deleteAllByTenantId(tenantId);

			// 5. Borrar clientes
			clienteRepository.deleteAllByTenantId(tenantId);

			// 6. Borrar productos
			productoRepository.deleteAllByTenantId(tenantId);

			// 7. Borrar suscripción (FK a user) ⚠️ ANTES del user
			suscripcionRepository.deleteByUserId(userId);

			// 7.5 Borrar invitaciones			
			invitacionRepository.deleteAllByTenantId(tenantId);
			
			// 8. Borrar usuario
			userRepository.delete(user);

			// 9. Borrar tenant
			tenantRepository.deleteById(tenantId);

			return ResponseEntity.ok("Usuario eliminado correctamente");

		} catch (Exception e) {
		    e.printStackTrace();
		    return ResponseEntity.status(500)
		        .body("Error al eliminar: " + e.getMessage());
		}
	}

	@DeleteMapping("/cleanup")
	public ResponseEntity<?> cleanup(HttpServletRequest request) {
		// Solo llega acá si el token Firebase es válido
		// Si el UID no existe en BD → no hay nada que limpiar
		// Si existe → eliminarlo (caso donde se re-registró en Firebase)
		String uid = (String) request.getAttribute("firebaseUid");
		userRepository.findByFirebaseUid(uid).ifPresent(user -> {
			tenantRepository.deleteById(user.getTenant().getId());
			userRepository.delete(user);
		});
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/puede-agregar-empleado")
	public ResponseEntity<Boolean> puedeAgregarEmpleado(HttpServletRequest request) {
	    return ResponseEntity.ok(authService.puedeAgregarEmpleado(request));
	}
}
