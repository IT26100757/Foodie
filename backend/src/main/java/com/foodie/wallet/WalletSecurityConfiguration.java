package com.foodie.wallet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WalletSecurityConfiguration {
    @Bean
    SecurityFilterChain walletSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/wallet/**"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/wallet/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
