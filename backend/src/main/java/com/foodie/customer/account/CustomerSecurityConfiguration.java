package com.foodie.customer.account;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration(proxyBeanMethods = false)
public class CustomerSecurityConfiguration {

    @Bean
    @Order(1)
    public SecurityFilterChain customerSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("customerSecurityContextRepository")
            SecurityContextRepository securityContextRepository,
            @Qualifier("customerCsrfTokenRepository")
            CsrfTokenRepository csrfTokenRepository
    ) throws Exception {

        http
                .securityMatcher(
                        "/api/customer/auth/**",
                        "/api/customer/account/**"
                )
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR)
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/customer/auth/csrf"
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/customer/auth/register",
                                "/api/customer/auth/login"
                        )
                        .permitAll()
                        .anyRequest()
                        .hasRole("CUSTOMER")
                )
                .securityContext(context -> context
                        .securityContextRepository(
                                securityContextRepository
                        )
                        .requireExplicitSave(true)
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )
                        .accessDeniedHandler(
                                (request, response, exception) ->
                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN
                                        )
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/api/customer/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(
                                (request, response, authentication) ->
                                        response.setStatus(
                                                HttpServletResponse.SC_NO_CONTENT
                                        )
                        )
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain fallbackSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR)
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .csrf(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}