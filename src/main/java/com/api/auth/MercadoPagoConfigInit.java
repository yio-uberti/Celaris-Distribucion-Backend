package com.api.auth;

import org.springframework.context.annotation.Configuration;

import com.mercadopago.MercadoPagoConfig;

@Configuration
public class MercadoPagoConfigInit {
	public MercadoPagoConfigInit() {
		MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));
	}
//	public MercadoPagoConfigInit() {
//        MercadoPagoConfig.setAccessToken("APP_USR-7287397855536710-120117-87ceb2b05bc67aefd0740dcf98e0e895-543427993");
//    }

}
