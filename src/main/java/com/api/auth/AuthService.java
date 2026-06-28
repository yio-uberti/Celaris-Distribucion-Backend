package com.api.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.entidad.ProductoCatalogo;
import com.api.entidad.productos;
import com.api.referencia.OrigenReferencia;
import com.api.referencia.OrigenReferenciaRepository;
import com.api.repositorio.ProductoCatalogoRepository;
import com.api.repositorio.repoProducto;
import com.api.tenant.Tenant;
import com.api.tenant.TenantRepository;
import com.api.user.InvitacionEmpleado;
import com.api.user.InvitacionRepository;
import com.api.user.RolRepositorio;
import com.api.user.Rubro;
import com.api.user.RubroRepository;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private ProductoCatalogoRepository productoCatalogoRepository;
	@Autowired
	private repoProducto productoRepository;
	@Autowired
	private RubroRepository rubroRepository;
	@Autowired
	private RolRepositorio rolRepository;
	@Autowired
	private InvitacionRepository invitacionRepository;
	@Autowired
	private OrigenReferenciaRepository origenReferenciaRepository;

	// Exponé getTenantId como público
	public Long getTenantIdPublic(HttpServletRequest request) {
		return getTenantId(request);
	}

	private Long getTenantId(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(uid).orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
				.getTenant().getId();
	}

	public boolean registerIfNotExists(HttpServletRequest request) {

		String firebaseUid = (String) request.getAttribute("firebaseUid");
		String email = (String) request.getAttribute("email");

		System.out.println("UID:" + firebaseUid);
		System.out.println("email:" + email);

		if (firebaseUid == null || email == null)
			return false;
		// Si ya existe en BD → OK, no hacer nada
		if (userRepository.existsByFirebaseUid(firebaseUid))
			return false;

		// Si llegó acá, Firebase validó el token (el filtro JWT ya lo hizo)
		// pero no existe en BD → crearlo
		Tenant tenant = new Tenant();
		tenant.setActivo(true);
		tenant.setFechaAlta(LocalDateTime.now());
		tenant.setTipo("PENDIENTE");
		System.out.println("Proveedor:" + email);
		tenantRepository.save(tenant);

		User user = new User();
		user.setFirebaseUid(firebaseUid);
		user.setEmail(email);
		user.setTenant(tenant);
		// rol OWNER por defecto (id=1)
		user.setRol(rolRepository.findById(1).orElseThrow());
		userRepository.save(user);

		return true;
	}

	@Transactional
	public void completarFormulario(HttpServletRequest request, FormularioRequest formulario) {
		String firebaseUid = (String) request.getAttribute("firebaseUid");
	    if (firebaseUid == null)
	        return;
	    
	    Optional<User> userOptional = userRepository.findByFirebaseUid(firebaseUid);
	    if (userOptional.isEmpty()) {
	        throw new RuntimeException("Usuario no encontrado");
	    }
	    
	    User userData = userOptional.get();
	    userData.setNombre(formulario.getNombre());
	    userData.setApellido(formulario.getApellido());
	    userData.setEdad(formulario.getEdad());
	    userData.setFrecuenciaDeudores(formulario.getFrecuenciaDeudores());
	    
	    Tenant tenant = userData.getTenant();
	    
	    // 🔥 ASIGNAR EL TIPO DESDE EL FORMULARIO
	    tenant.setTipo(formulario.getTipo());
	    tenant.setNombre(formulario.getNombre()); // Conectar el nombre del usuario al tenant
	    
	    if ("EMPRESA".equals(formulario.getTipo())) {
	        tenant.setNombreFantasia(formulario.getNombreFantasia());
	        tenant.setRazonSocial(formulario.getRazonSocial());
	        tenant.setCuit(formulario.getCuit());
	        tenant.setTelefono(formulario.getTelefono());
	    } else if ("AUTONOMO".equals(formulario.getTipo())) {
	        userData.setRubro(formulario.getRubro());
	        
	        // Guardar el rubro si no existe
	        if (rubroRepository.findByNombre(formulario.getRubro()).isEmpty()) {
	            Rubro r = new Rubro();
	            r.setNombre(formulario.getRubro());
	            rubroRepository.save(r);
	        }
	        asignarProductosCatalogo(userData);
	    }
		
		// 🔥 GUARDAR ORIGEN DE REFERENCIA
	    if (formulario.getOrigenReferencia() != null) {
	        Optional<OrigenReferencia> origenExistente = origenReferenciaRepository
	            .findByUsuarioId(userData.getId());

	        if (origenExistente.isPresent()) {
	            // ✅ Actualizar existente
	            OrigenReferencia origen = origenExistente.get();
	            origen.setOrigen(
	                formulario.getOrigenReferencia().equals("otro") 
	                    ? "otro" 
	                    : formulario.getOrigenReferencia()
	            );
	            origen.setOrigenPersonalizado(
	                formulario.getOrigenReferencia().equals("otro")
	                    ? formulario.getOrigenReferencia()
	                    : null
	            );
	            origenReferenciaRepository.save(origen);
	        } else {
	            // ✅ Crear nuevo
	            OrigenReferencia origen = OrigenReferencia.builder()
	                .usuarioId(userData.getId())
	                .origen(
	                    formulario.getOrigenReferencia().equals("otro") 
	                        ? "otro" 
	                        : formulario.getOrigenReferencia()
	                )
	                .origenPersonalizado(
	                    formulario.getOrigenReferencia().equals("otro")
	                        ? formulario.getOrigenReferencia()
	                        : null
	                )
	                .build();
	            
	            origenReferenciaRepository.save(origen);
	        }
	    }

		tenantRepository.save(tenant);
		userRepository.save(userData);

	}

	private void asignarProductosCatalogo(User user) {
		// Buscar el rubro del usuario
		Optional<Rubro> rubroOpt = rubroRepository.findByNombre(user.getRubro());
		if (!rubroOpt.isPresent())
			return;

		Rubro rubro = rubroOpt.get();

		// Obtener productos del catálogo de ese rubro
		List<ProductoCatalogo> productosCatalogo = productoCatalogoRepository.findByRubro(rubro);

		// Crear productos para el tenant del usuario
		for (ProductoCatalogo catalogo : productosCatalogo) {
			productos producto = productos.builder().nombreProducto(catalogo.getNombre()).precioActual(null) // Sin
																												// precio
																												// inicialmente
					.tenantId(user.getTenant().getId()).catalogo(catalogo).personalizado(false).build();

			productoRepository.save(producto);
		}
	}

	public boolean tieneProductosSinPrecio(HttpServletRequest request) {
		String firebaseUid = (String) request.getAttribute("firebaseUid");
		if (firebaseUid == null)
			return true; // Sin auth = bloquear

		Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
		if (!userOpt.isPresent())
			return true;

		User user = userOpt.get();
		Long tenantId = user.getTenant().getId();

		// Contar productos sin precio del tenant
		long productosSinPrecio = productoRepository.countByTenantIdAndPrecioActualIsNull(tenantId);

		return productosSinPrecio > 0; // true = hay productos sin precio
	}

	public void activarInvitacion(HttpServletRequest request, ActivarInvitacionRequest body) {
		String firebaseUid = (String) request.getAttribute("firebaseUid");
		String email = (String) request.getAttribute("email");

		// Buscar invitación válida y pendiente
		InvitacionEmpleado invitacion = invitacionRepository.findByTokenAndEstado(body.getToken(), "PENDIENTE")
				.orElseThrow(() -> new RuntimeException("Invitación inválida o ya usada"));

		// Verificar que no esté vencida
		if (invitacion.getVenceEn() != null && invitacion.getVenceEn().isBefore(LocalDateTime.now())) {
			invitacion.setEstado("VENCIDA");
			invitacionRepository.save(invitacion);
			throw new RuntimeException("Invitación vencida");
		}

		// Crear el user empleado dentro del tenant de la empresa
		User empleado = new User();
		empleado.setFirebaseUid(firebaseUid);
		empleado.setEmail(email);
		empleado.setNombre(body.getNombre());
		empleado.setApellido(body.getApellido());
		empleado.setDni(body.getDni());
		empleado.setTenant(invitacion.getTenant()); // mismo tenant que la empresa
		empleado.setRol(invitacion.getRol());
		empleado.setActivo(true);
		userRepository.save(empleado);

		// Marcar invitación como aceptada
		invitacion.setEstado("ACEPTADA");
		invitacionRepository.save(invitacion);
	}

	public boolean puedeAgregarEmpleado(HttpServletRequest request) {
		Long tenantId = getTenantId(request);
		long cantidadEmpleados = userRepository.countByTenantIdAndRol_NombreNot(tenantId, "OWNER");
		return cantidadEmpleados < 3;
	}
}
