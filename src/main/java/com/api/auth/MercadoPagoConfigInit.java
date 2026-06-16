package com.api.auth;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.mercadopago.MercadoPagoConfig;

import jakarta.annotation.PostConstruct;

@Configuration
public class MercadoPagoConfigInit {
	
	@Value("${MP_ACCESS_TOKEN:}")
    private String accessToken;
    
    @PostConstruct
    public void init() {
        // Intenta de env var si no está en properties
        if (accessToken == null || accessToken.trim().isEmpty()) {
            accessToken = System.getenv("MP_ACCESS_TOKEN");
        }
        
        // Validaciones
        if (accessToken == null || accessToken.trim().isEmpty()) {
            PrintStream out = System.out;
            out.println("════════════════════════════════════════");
            out.println("❌ CRÍTICO: MP_ACCESS_TOKEN no configurado");
            out.println("════════════════════════════════════════");
            throw new RuntimeException("MP_ACCESS_TOKEN no encontrado en env vars ni en properties");
        }
        
        // Configurar en MP SDK
        MercadoPagoConfig.setAccessToken(accessToken.trim());
        
        // Mostrar información
        PrintStream out = System.out;
        out.println("\n════════════════════════════════════════");
        out.println("✅ MercadoPago Configurado");
        out.println("════════════════════════════════════════");
        out.println("Token: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
        
        String ambiente = accessToken.startsWith("TEST_") 
            ? "🔴 SANDBOX (solo tarjetas de prueba)"
            : accessToken.startsWith("APP_USR_")
            ? "🟢 PRODUCCIÓN (tarjetas reales)"
            : "❓ DESCONOCIDO";
            
        out.println("Ambiente: " + ambiente);
        out.println("════════════════════════════════════════\n");
    }

}
