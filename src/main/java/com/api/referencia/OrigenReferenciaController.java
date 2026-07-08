package com.api.referencia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.user.User;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/control/referencia")
public class OrigenReferenciaController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrigenReferenciaRepository origenReferenciaRepository;

    /**
     * 🔥 Obtener estadísticas de origen de referencia
     * Solo accesible para Giorgio
     */
    @GetMapping("/reportes/origenes")
    public ResponseEntity<?> reporteOrigenes(HttpServletRequest request) {
        try {
            String firebaseUid = (String) request.getAttribute("firebaseUid");
            
            User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // ✅ Verificar que sea OWNER o ADMIN
            String rol = user.getRol().getNombre();
            if (!rol.equals("OWNER") && !rol.equals("ADMIN")) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Solo propietarios pueden ver este reporte"));
            }

            // Obtener orígenes de referencia
            List<OrigenReferencia> origenes = origenReferenciaRepository.findAll();

            // 🔥 Calcular estadísticas
            Map<String, Integer> estadisticas = new HashMap<>();
            estadisticas.put("instagram", 0);
            estadisticas.put("familiares", 0);
            estadisticas.put("tarjeta", 0);
            estadisticas.put("otro", 0);

            List<Map<String, Object>> detalleOtros = new ArrayList<>();

            for (OrigenReferencia origen : origenes) {
                String origenNombre = origen.getOrigen();
                
                if (origenNombre == null || !estadisticas.containsKey(origenNombre)) {
                    estadisticas.put("otro", estadisticas.get("otro") + 1);
                } else {
                    estadisticas.put(origenNombre, estadisticas.get(origenNombre) + 1);
                }

                if ("otro".equals(origenNombre) && origen.getOrigenPersonalizado() != null) {
                    detalleOtros.add(Map.of(
                        "fecha", origen.getFechaRegistro(),
                        "detalle", origen.getOrigenPersonalizado()
                    ));
                }
            }

            // 🔥 Respuesta estructurada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("total", origenes.size());
            respuesta.put("estadisticas", estadisticas);
            respuesta.put("detalleOtros", detalleOtros);
            respuesta.put("porcentajes", calcularPorcentajes(estadisticas, origenes.size()));

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Calcular porcentajes
     */
    private Map<String, Double> calcularPorcentajes(Map<String, Integer> estadisticas, int total) {
        Map<String, Double> porcentajes = new HashMap<>();
        
        if (total == 0) {
            estadisticas.keySet().forEach(key -> porcentajes.put(key, 0.0));
            return porcentajes;
        }

        estadisticas.forEach((key, value) -> {
            double porcentaje = (value * 100.0) / total;
            porcentajes.put(key, Math.round(porcentaje * 100.0) / 100.0);
        });

        return porcentajes;
    }
}