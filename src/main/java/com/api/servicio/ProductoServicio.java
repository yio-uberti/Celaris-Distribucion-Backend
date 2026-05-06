package com.api.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.entidad.productos;
import com.api.repositorio.repoProducto;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class ProductoServicio {

	@Autowired
	private repoProducto productoRepository;
	@Autowired
	private UserRepository userRepository;

	// Helper para obtener tenantId del request
	private Long getTenantId(HttpServletRequest request) {
		String firebaseUid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(firebaseUid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado")).getTenant().getId();
	}

	public List<productos> getAll(HttpServletRequest request) {
		return productoRepository.findAllByTenantId(getTenantId(request));
	}

	public productos create(HttpServletRequest request, productos producto) {
		producto.setTenantId(getTenantId(request));
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
