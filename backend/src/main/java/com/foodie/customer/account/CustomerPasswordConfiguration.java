package com.foodie.customer.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class CustomerPasswordConfiguration {

    @Bean(name = "customerPasswordEncoder")
    public PasswordEncoder customerPasswordEncoder() {

        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(
                "",
                16,
                600_000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256
        );

        String encodingId = "pbkdf2-sha256-600000";

        return new DelegatingPasswordEncoder(
                encodingId,
                Map.of(encodingId, encoder)
        );
    }
}