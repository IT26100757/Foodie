package com.foodie.customer.account;

import java.time.Instant;

public record CustomerAccountResponse(
        Long id,
        String firstName,
        String lastName,
        String displayName,
        String email,
        String phone,
        Instant createdAt
) {

    public static CustomerAccountResponse from(CustomerAccount account) {
        return new CustomerAccountResponse(
                account.getId(),
                account.getFirstName(),
                account.getLastName(),
                account.getFirstName() + " " + account.getLastName(),
                account.getEmail(),
                account.getPhone(),
                account.getCreatedAt()
        );
    }
}