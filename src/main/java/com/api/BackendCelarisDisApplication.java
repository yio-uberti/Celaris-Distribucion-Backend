package com.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendCelarisDisApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendCelarisDisApplication.class, args);
	}

}
