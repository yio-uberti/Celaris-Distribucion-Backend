package com.api.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.entidad.ProductoCatalogo;
import com.api.entidad.productos;
import com.api.repositorio.ProductoCatalogoRepository;
import com.api.repositorio.repoProducto;
import com.api.user.Rubro;
import com.api.user.RubroRepository;
import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServicio {

	@Autowired
	private repoProducto productoRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired 
	private ProductoCatalogoRepository productoCatalogoRepository;
	@Autowired
	private RubroRepository rubroRepository;

	
	private final PlanLimiteService planLimiteService;

	// Helper para obtener tenantId del request
	private Long getTenantId(HttpServletRequest request) {
		String firebaseUid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(firebaseUid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado")).getTenant().getId();
	}

//	GET para traer todos los productos registrados
	public List<productos> getAll(HttpServletRequest request) {
		return productoRepository.findAllByTenantId(getTenantId(request));
	}
//	GET para traer todos los producto categorizados por marca y categoria
	public List<productos> getAllcategorizados(HttpServletRequest request){
		return productoRepository.listaCategorizada(getTenantId(request));
	}

	
	public List<productos> buscarPorNombre(HttpServletRequest request, String query) {
	    Long tenantId = getTenantId(request);
	    if (query == null || query.trim().length() < 2) {
	        return List.of(); // no busques con 0-1 letra, es ruido y carga al pedo
	    }
	    return productoRepository.buscarPorNombre(tenantId, query.trim());
	}
	
	private static final int LIMITE_PRODUCTOS_FREE = 1000;

	public productos create(HttpServletRequest request, productos producto) {
	    String firebaseUid = (String) request.getAttribute("firebaseUid");
	    User user = userRepository.findByFirebaseUid(firebaseUid)
	        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	    Long tenantId = user.getTenant().getId();
	    producto.setTenantId(tenantId);
	    
	    if (!planLimiteService.tenantEsPremium(tenantId)) {
	        long totalActual = productoRepository.countByTenantId(tenantId);
	        if (totalActual >= LIMITE_PRODUCTOS_FREE) {
	            throw new LimitePlanException(
	                "Alcanzaste el límite de " + LIMITE_PRODUCTOS_FREE +
	                " productos de tu plan Free. Actualizá a Premium para seguir cargando."
	            );
	        }
	    }
	    // Buscar el rubro del usuario
	    Optional<Rubro> rubroOpt = rubroRepository.findByNombre(user.getRubro());

	    if (rubroOpt.isPresent()) {
	        // Vincularlo al catálogo si ya existe
	        Optional<ProductoCatalogo> catalogoOpt = productoCatalogoRepository
	            .findByNombreAndRubroNombre(producto.getNombreProducto(), user.getRubro());

	        if (catalogoOpt.isPresent()) {
	            // Ya está en el catálogo, solo vinculamos
	            producto.setCatalogo(catalogoOpt.get());
	            producto.setPersonalizado(false);
	        } else {
	            // No está en el catálogo → lo agregamos silenciosamente
	            ProductoCatalogo nuevo = new ProductoCatalogo();
	            nuevo.setNombre(producto.getNombreProducto());
	            nuevo.setRubro(rubroOpt.get());
	            nuevo.setTipoPrecio(producto.getTipoPrecio());
	            ProductoCatalogo guardado = productoCatalogoRepository.save(nuevo);
	            producto.setCatalogo(guardado);
	            producto.setPersonalizado(false);
	        }
	    } else {
	        // El rubro del usuario no existe en la BD (rubro muy nuevo)
	        producto.setPersonalizado(true);
	    }

	    return productoRepository.save(producto);
	}

	public productos update(HttpServletRequest request, String nombre, productos datos) {
		Long tenantId = getTenantId(request);
		productos producto = productoRepository.findByNombreProductoAndTenantId(nombre, tenantId)
				.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

		producto.setPrecioActual(datos.getPrecioActual());
		return productoRepository.save(producto);
	}

	
//	Metodo para borrar un producto
	@Transactional
	public void delete(HttpServletRequest request, String nombre) {
		Long tenantId = getTenantId(request);
		productoRepository.findByNombreProductoAndTenantId(nombre, tenantId)
				.orElseThrow(() -> new RuntimeException("Producto no encontrado"));
		productoRepository.deleteByNombreProductoAndTenantId(nombre, tenantId);
	}
}
