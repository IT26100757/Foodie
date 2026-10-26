package com.foodie.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {
    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false, length = 40)
    private String transactionId;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_holder", nullable = false, length = 100)
    private String accountHolder;

    @Column(name = "account_last_four", nullable = false, length = 4)
    private String accountLastFour;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "LKR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WalletTransferStatus status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WalletTransaction() {
    }

    public WalletTransaction(String transactionId, String bankName, String accountHolder,
                             String accountLastFour, BigDecimal amount) {
        this.transactionId = transactionId;
        this.bankName = bankName;
        this.accountHolder = accountHolder;
        this.accountLastFour = accountLastFour;
        this.amount = amount;
        this.currency = "LKR";
        this.status = WalletTransferStatus.PROCESSING;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getTransactionId() { return transactionId; }
    public String getBankName() { return bankName; }
    public String getAccountHolder() { return accountHolder; }
    public String getAccountLastFour() { return accountLastFour; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public WalletTransferStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setStatus(WalletTransferStatus status) { this.status = status; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
