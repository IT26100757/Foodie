package com.foodie.customer.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@Validated
public class CustomerRegistrationService {

    private final CustomerAccountRepository accountRepository;
    private final CustomerAddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerRegistrationService(
            CustomerAccountRepository accountRepository,
            CustomerAddressRepository addressRepository,
            @Qualifier("customerPasswordEncoder")
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerAccountResponse register(
            @NotNull @Valid RegisterRequest request
    ) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match."
            );
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String phone = request.phone();

        if (accountRepository.existsByEmailIgnoreCase(email)
                || accountRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email or phone number already exists."
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());

        CustomerAccount account = new CustomerAccount(
                request.firstName().trim(),
                request.lastName().trim(),
                request.nationalId().trim().toUpperCase(Locale.ROOT),
                email,
                phone,
                passwordHash
        );

        CustomerAccount savedAccount;

        try {
            savedAccount = accountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException exception) {
            // Database constraints also protect against simultaneous
            // registration requests using the same email or phone.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Registration conflicts with existing account data."
            );
        }

        CustomerAddress firstAddress = new CustomerAddress(
                savedAccount,
                request.address1().trim(),
                request.address2().trim(),
                request.address3().trim(),
                null,
                null,
                true
        );

        addressRepository.saveAndFlush(firstAddress);

        return CustomerAccountResponse.from(savedAccount);
    }
}