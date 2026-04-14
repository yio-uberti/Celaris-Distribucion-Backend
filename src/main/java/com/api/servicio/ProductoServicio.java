package com.api.servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.controlador.ActualizacionResultado;
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
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado")).getTenantId();
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

		producto.setPrecio_actual(datos.getPrecio_actual());
		return productoRepository.save(producto);
	}

//	Metodo para actualizacion colectiva
	public ActualizacionResultado actualizacionMasiva(HttpServletRequest request, String texto) {
	    Long tenantId = getTenantId(request);

	    List<productos> lista = productoRepository.findAllByTenantId(tenantId);

	    ActualizacionResultado res = new ActualizacionResultado();
	    res.actualizados = new ArrayList<>();
	    res.noEncontrados = new ArrayList<>();
	    res.precioMenor = new ArrayList<>();

	    String[] items = texto.split(",");

	    for (String item : items) {
	        try {
	            String[] partes = item.split(":");
	            String nombreInput = normalizar(partes[0]);
	            BigDecimal precioNuevo = new BigDecimal(partes[1].trim());

	            productos encontrado = buscarProducto(lista, nombreInput);

	            if (encontrado == null) {
	                res.noEncontrados.add(partes[0]);
	                continue;
	            }

	            if (precioNuevo.compareTo(encontrado.getPrecio_actual()) < 0) {
	                res.precioMenor.add(partes[0]);
	                continue;
	            }

	            encontrado.setPrecio_actual(precioNuevo);
	            productoRepository.save(encontrado);

	            res.actualizados.add(encontrado.getNombreProducto());

	        } catch (Exception e) {
	            res.noEncontrados.add(item);
	        }
	    }

	    return res;
	}
	
//	Metodos auxiliares para la actualizacion colectiva
	private productos buscarProducto(List<productos> lista, String inputNormalizado) {
	    for (productos p : lista) {
	        String nombreBD = normalizar(p.getNombreProducto());

	        if (nombreBD.contains(inputNormalizado) || inputNormalizado.contains(nombreBD)) {
	            return p;
	        }
	    }
	    return null;
	}
	
	public String normalizar(String texto) {
	    return texto.toLowerCase()
	        .replace(" ", "")
	        .replace("-", "")
	        .replace("litros", "l")
	        .replace("litro", "l")
	        .replace("ml", "")
	        .replace("cc", "")
	        .replace("1500", "1.5")
	        .replace("1000", "1");
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
