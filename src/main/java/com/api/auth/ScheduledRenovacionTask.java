package com.api.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
 
@Component
public class ScheduledRenovacionTask {
 
    @Autowired
    private MercadoPagoController mercadoPagoController;
 
    // ✅ Se ejecuta CADA DÍA a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *", zone = "America/Argentina/Buenos_Aires")
    public void ejecutarRenovaciones() {
        System.out.println("\n⏰ INICIANDO TAREA PROGRAMADA DE RENOVACIONES");
        
        try {
            mercadoPagoController.cobrarRenovacionesVencidas();
        } catch (Exception e) {
            System.out.println("❌ Error en tarea de renovaciones: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
 
    // Si quieres una ejecución cada 1 hora para testing:
    // @Scheduled(fixedRate = 3600000) // cada 1 hora
    // public void ejecutarRenovacionesCadaHora() { ... }
}
