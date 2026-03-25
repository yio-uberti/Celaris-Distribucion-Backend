package com.api.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.entidad.clientes;
import com.api.repositorio.repoClientes;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ClienteServicio {

	@Autowired
	private repoClientes clienteRepository;
	@Autowired
	private UserRepository userRepository;

	// Metodo de seguridad para verificar existencia
	private Long getTenantId(HttpServletRequest request) {
		String uid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(uid).orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
				.getTenantId();
	}

	// Metodo parar traer todos los clientes
	public List<clientes> getAll(HttpServletRequest request) {
		return (List<clientes>) clienteRepository.findAllByTenantId(getTenantId(request));
	}

	// Busca por nombre, si no existe lo crea — usado internamente en VentaService
	public clientes findOrCreate(String nombreCliente, Long tenantId) {
		return clienteRepository.findByNombreClienteAndTenantId(nombreCliente, tenantId)
				.orElseGet(() -> clienteRepository
						.save(clientes.builder().nombreCliente(nombreCliente).tenantId(tenantId).build()));
	}
}