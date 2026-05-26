package com.api.controlador;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ActivarController {

    @GetMapping("/activar")
    public ResponseEntity<String> activar(
            @RequestParam String token,
            HttpServletResponse response) throws IOException {

        String deepLink = "celaris-distribuciones://activar?token=" + token;
        String playStore = "https://play.google.com/store/apps/details?id=com.celaris.distribuciones";

        String html = """
            <html>
            <head>
              <meta charset="UTF-8">
              <title>Activar cuenta - Celaris</title>
            </head>
            <body>
              <p>Abriendo la app...</p>
              <script>
                window.location = '%s';
                setTimeout(function() {
                  window.location = '%s';
                }, 2500);
              </script>
              <p>¿No abrió automáticamente? 
                <a href="%s">Tocá acá para abrir la app</a> o 
                <a href="%s">descargala desde Play Store</a>
              </p>
            </body>
            </html>
            """.formatted(deepLink, playStore, deepLink, playStore);

        return ResponseEntity.ok()
            .header("Content-Type", "text/html; charset=UTF-8")
            .body(html);
    }
}