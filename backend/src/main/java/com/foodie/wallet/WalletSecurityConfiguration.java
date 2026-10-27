package com.foodie.wallet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WalletSecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain walletSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/wallet/**")
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/wallet/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}