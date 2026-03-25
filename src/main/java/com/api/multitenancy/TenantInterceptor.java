package com.api.multitenancy;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantInterceptor implements HandlerInterceptor {
	
	private final UserRepository userRepository;

    public TenantInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        
        // Obtener tenant del header o token
        String tenantId = request.getHeader("X-Tenant-ID");
        
        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        TenantContext.clear();
    }
}
