package com.api.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.api.user.User;
import com.api.user.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

	@Autowired
	private UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {

			String token = authHeader.substring(7);

			try {
			    FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

			    String firebaseUid = decodedToken.getUid();
			    request.setAttribute("firebaseUid", firebaseUid);
			    request.setAttribute("email", decodedToken.getEmail());

			    // Solo buscar el usuario si ya existe (para login)
			    // Para register, puede no existir todavía
			    Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);

			    if (userOpt.isPresent()) {
			        UsernamePasswordAuthenticationToken authentication =
			            new UsernamePasswordAuthenticationToken(userOpt.get(), null, List.of());
			        SecurityContextHolder.getContext().setAuthentication(authentication);
			    }
			    // Si no existe, dejamos pasar igual para que /register lo cree

			} catch (FirebaseAuthException e) {
			    // Solo rechazar si el TOKEN de Firebase es inválido
			    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			    return;
			} catch (Exception e) {
			    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			    return;
			}
		}

		filterChain.doFilter(request, response);
	}

}
