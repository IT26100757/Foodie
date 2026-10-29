package com.foodie.wallet;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransferResponse(
        String transactionId,
        String bankName,
        String accountHolder,
        String accountLastFour,
        BigDecimal amount,
        String currency,
        WalletTransferStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    static WalletTransferResponse from(WalletTransaction transaction) {
        return new WalletTransferResponse(
                transaction.getTransactionId(),
                transaction.getBankName(),
                transaction.getAccountHolder(),
                transaction.getAccountLastFour(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getFailureReason(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
