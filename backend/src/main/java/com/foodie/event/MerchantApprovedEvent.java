package com.foodie.event;

public record MerchantApprovedEvent(
        String email,
        String firstName
) {
}
