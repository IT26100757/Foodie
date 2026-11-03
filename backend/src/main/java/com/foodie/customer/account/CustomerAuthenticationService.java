package com.foodie.customer.account;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerAuthenticationService {

    private final AuthenticationManager authenticationManager;

    public CustomerAuthenticationService(
            CustomerAccountRepository accountRepository,
            @Qualifier("customerPasswordEncoder")
            PasswordEncoder passwordEncoder
    ) {
        CustomerUserDetailsService userDetailsService =
                new CustomerUserDetailsService(accountRepository);

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(true);

        this.authenticationManager = new ProviderManager(provider);
    }

    public Authentication authenticate(
            String identifier,
            String password
    ) {
        UsernamePasswordAuthenticationToken authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        identifier,
                        password
                );

        try {
            return authenticationManager.authenticate(
                    authenticationRequest
            );
        } finally {
            authenticationRequest.eraseCredentials();
        }
    }
}