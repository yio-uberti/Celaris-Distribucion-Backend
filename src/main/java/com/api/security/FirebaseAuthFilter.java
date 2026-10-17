package com.api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.api.servicio.HorarioServicio;
import com.api.user.User;
import com.api.user.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

	@Autowired private UserRepository userRepository;
	@Autowired private HorarioServicio horarioServicio;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			FirebaseToken decoded;
			try {
				decoded = FirebaseAuth.getInstance().verifyIdToken(authHeader.substring(7));
			} catch (Exception e) {            // solo el token inválido es 401
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			String uid = decoded.getUid();
			request.setAttribute("firebaseUid", uid);
			request.setAttribute("email", decoded.getEmail());

			User user = userRepository.findByFirebaseUid(uid).orElse(null);
			if (user != null) {
				if (Boolean.FALSE.equals(user.getActivo())) {
					rechazar(response, "Cuenta deshabilitada");
					return;
				}
				if (!horarioServicio.puedeEntrar(user.getId())) {
					rechazar(response, "Fuera de tu horario de trabajo");
					return;
				}
			}
			// Si no existe, pasa igual para que /register lo cree

			SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken(uid, null, List.of()));
		}
		chain.doFilter(request, response);
	}

	private void rechazar(HttpServletResponse response, String msg) throws IOException {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"error\": \"" + msg + "\"}");
	}
}
