package com.api.auth;

import org.springframework.context.annotation.Configuration;

import com.mercadopago.MercadoPagoConfig;

@Configuration
public class MercadoPagoConfigInit {

	public MercadoPagoConfigInit() {
        MercadoPagoConfig.setAccessToken("TEST-7287397855536710-120117-6dce583016b00defca5345e2d18b942b-543427993");
    }
}
