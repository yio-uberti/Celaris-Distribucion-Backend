package com.api.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.entidad.ProductoCatalogo;
import com.api.entidad.productos;
import com.api.repositorio.ProductoCatalogoRepository;
import com.api.repositorio.repoProducto;
import com.api.tenant.Tenant;
import com.api.tenant.TenantRepository;
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


    public boolean registerIfNotExists(HttpServletRequest request) {

        String firebaseUid = (String) request.getAttribute("firebaseUid");
        String email = (String) request.getAttribute("email");

        System.out.println("UID:" + firebaseUid);
        System.out.println("email:" + email);
        
        if (firebaseUid == null || email == null) return false ;

        // Si ya existe en BD → OK, no hacer nada
        if (userRepository.existsByFirebaseUid(firebaseUid)) return false;

        // Si llegó acá, Firebase validó el token (el filtro JWT ya lo hizo)
        // pero no existe en BD → crearlo
        Tenant tenant = tenantRepository.save(
            new Tenant("Proveedor " + email)
        );

        User user = new User();
        user.setFirebaseUid(firebaseUid);
        user.setEmail(email);
        user.setTenantId(tenant.getId());
        userRepository.save(user);
        
        return true;
    }

    public void completarFormulario(HttpServletRequest request, FormularioRequest formulario) {
        String firebaseUid = (String) request.getAttribute("firebaseUid");
        if (firebaseUid == null) return;
        
        Optional<User> userOptional = userRepository.findByFirebaseUid(firebaseUid);
        User userData = userOptional.get();
        
        
        if (userData == null) return;
        
        userData.setNombre(formulario.getNombre());
        userData.setEdad(formulario.getEdad());
        userData.setRubro(formulario.getRubro());
        userData.setFrecuenciaDeudores(formulario.getFrecuenciaDeudores());
        
        if (!"Personalizado".equals(formulario.getRubro())) {
			asignarProductosCatalogo(userData);
		}
        
        userRepository.save(userData);
    }
    
    private void asignarProductosCatalogo(User user) {
        // Buscar el rubro del usuario
        Optional<Rubro> rubroOpt = rubroRepository.findByNombre(user.getRubro());
        if (!rubroOpt.isPresent()) return;
        
        Rubro rubro = rubroOpt.get();
        
        // Obtener productos del catálogo de ese rubro
        List<ProductoCatalogo> productosCatalogo = 
            productoCatalogoRepository.findByRubro(rubro);
        
        // Crear productos para el tenant del usuario
        for (ProductoCatalogo catalogo : productosCatalogo) {
            productos producto = productos.builder()
                .nombreProducto(catalogo.getNombre())
                .precioActual(null) // Sin precio inicialmente
                .tenantId(user.getTenantId())
                .catalogo(catalogo)
                .personalizado(false)
                .build();
            
            productoRepository.save(producto);
        }
    }
    
    public boolean tieneProductosSinPrecio(HttpServletRequest request) {
        String firebaseUid = (String) request.getAttribute("firebaseUid");
        if (firebaseUid == null) return true; // Sin auth = bloquear
        
        Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
        if (!userOpt.isPresent()) return true;
        
        User user = userOpt.get();
        Long tenantId = user.getTenantId();
        
        // Contar productos sin precio del tenant
        long productosSinPrecio = productoRepository.countByTenantIdAndPrecioActualIsNull(tenantId);
        
        return productosSinPrecio > 0; // true = hay productos sin precio
    }
}
