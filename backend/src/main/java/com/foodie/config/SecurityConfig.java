package com.foodie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/merchants/register",
                                "/api/merchants/login"
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/merchants/register",
                                "/api/merchants/login"
                        ).permitAll()

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/csrf")
                        .permitAll()

                        .requestMatchers("/login", "/error")
                        .permitAll()

                        .requestMatchers("/api/merchant/**")
                        .hasRole("MERCHANT")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.permitAll());

        return http.build();
    }
}
