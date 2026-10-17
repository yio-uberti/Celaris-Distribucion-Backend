package com.api;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class BackendCelarisDisApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
		SpringApplication.run(BackendCelarisDisApplication.class, args);
		log.info("MAIL user={}, passLen={}", System.getenv("MAIL_USERNAME"), 
		         System.getenv("MAIL_PASSWORD") == null ? -1 : System.getenv("MAIL_PASSWORD").length());
	}

}
