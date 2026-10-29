package com.foodie.dto;

import com.foodie.model.Merchant;

public record PendingMerchantResponse(
        Long merchantId,
        String firstName,
        String lastName,
        String email,
        String verificationStatus
) {

    public static PendingMerchantResponse from(Merchant merchant) {
        return new PendingMerchantResponse(
                merchant.getId(),
                merchant.getFirstName(),
                merchant.getLastName(),
                merchant.getEmail(),
                merchant.getVerificationStatus().name()
        );
    }
}