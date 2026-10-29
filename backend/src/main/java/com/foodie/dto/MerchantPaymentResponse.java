
package com.foodie.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MerchantPaymentResponse(
        Long id,
        Long orderId,
        String paymentMethod,
        BigDecimal amount,
        String status,
        String provider,
        LocalDateTime createdAt
) {
}
