package com.foodie.customer.account;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Locale;
import java.util.Optional;

public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerAccountRepository accountRepository;

    public CustomerUserDetailsService(
            CustomerAccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        if (identifier == null || identifier.isBlank()) {
            throw invalidCredentials();
        }

        String value = identifier.trim();
        Optional<CustomerAccount> account;

        if (value.contains("@")) {
            String email = value.toLowerCase(Locale.ROOT);
            account = accountRepository.findByEmailIgnoreCase(email);
        } else {
            String phone = normalizePhone(value);
            account = accountRepository.findByPhone(phone);
        }

        return account
                .map(CustomerPrincipal::new)
                .orElseThrow(this::invalidCredentials);
    }

    private String normalizePhone(String value) {
        String phone = value.replaceAll("[\\s()-]", "");

        if (phone.matches("\\+94[0-9]{9}")) {
            return phone;
        }

        if (phone.matches("94[0-9]{9}")) {
            return "+" + phone;
        }

        if (phone.matches("0[0-9]{9}")) {
            return "+94" + phone.substring(1);
        }

        if (phone.matches("[0-9]{9}")) {
            return "+94" + phone;
        }

        throw invalidCredentials();
    }

    private UsernameNotFoundException invalidCredentials() {
        return new UsernameNotFoundException(
                "Invalid email or phone number or password."
        );
    }
}