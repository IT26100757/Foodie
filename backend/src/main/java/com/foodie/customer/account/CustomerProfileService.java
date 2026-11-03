package com.foodie.customer.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@Validated
public class CustomerProfileService {

    private final CustomerAccountRepository accountRepository;

    public CustomerProfileService(
            CustomerAccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public CustomerAccountResponse getProfile(
            @NotNull Long customerId
    ) {
        CustomerAccount account = findAccount(customerId);
        return CustomerAccountResponse.from(account);
    }

    @Transactional
    public CustomerAccountResponse updateProfile(
            @NotNull Long customerId,
            @NotNull @Valid UpdateProfileRequest request
    ) {
        CustomerAccount account = findAccount(customerId);

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String phone = request.phone().trim();

        boolean emailTaken = accountRepository
                .findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(customerId))
                .isPresent();

        boolean phoneTaken = accountRepository
                .findByPhone(phone)
                .filter(existing -> !existing.getId().equals(customerId))
                .isPresent();

        if (emailTaken || phoneTaken) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email or phone number is already in use."
            );
        }

        account.setFirstName(request.firstName().trim());
        account.setLastName(request.lastName().trim());
        account.setEmail(email);
        account.setPhone(phone);

        try {
            accountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Profile changes conflict with existing account data."
            );
        }

        return CustomerAccountResponse.from(account);
    }

    private CustomerAccount findAccount(Long customerId) {
        return accountRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Please sign in again."
                ));
    }
}