package com.api.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {
	
//	 @PostConstruct
//	    public void init() throws IOException {
//	       InputStream serviceAccount = 
//	    		   getClass().getClassLoader().getResourceAsStream("celarisdistribucion-firebase-adminsdk-fbsvc-5eeb28dd97.json");
//
//	        FirebaseOptions options = FirebaseOptions.builder()
//	            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//	            .build();
//
//	        if (FirebaseApp.getApps().isEmpty()) {
//	            FirebaseApp.initializeApp(options);
//	        }
//	    }
	 
//	 PRODUCCION EN EL SERVIDOR DE RENDER
	 @PostConstruct
	 public void init() throws IOException {
	     InputStream serviceAccount;
	     
	     String envJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
	     if (envJson != null) {
	         // Producción: viene de variable de entorno
	         serviceAccount = new ByteArrayInputStream(envJson.getBytes(StandardCharsets.UTF_8));
	     } else {
	         // Local: lee del archivo
	         serviceAccount = getClass().getClassLoader()
	             .getResourceAsStream("firebase-service-account.json");
	     }

	     FirebaseOptions options = FirebaseOptions.builder()
	         .setCredentials(GoogleCredentials.fromStream(serviceAccount))
	         .build();
	     if (FirebaseApp.getApps().isEmpty()) {
	         FirebaseApp.initializeApp(options);
	     }
	 }
}
