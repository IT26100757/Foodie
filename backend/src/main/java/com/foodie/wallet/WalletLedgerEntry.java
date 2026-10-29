
package com.foodie.wallet;

import com.foodie.model.Merchant;
import com.foodie.model.PaymentTransaction;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "wallet_ledger_entries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wallet_ledger_source_type",
                columnNames = {"payment_transaction_id", "entry_type"}
        )
)
public class WalletLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_transaction_id", nullable = false)
    private PaymentTransaction paymentTransaction;

    @Column(name = "entry_type", nullable = false, length = 30)
    private String entryType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "LKR";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WalletLedgerEntry() {
    }

    public WalletLedgerEntry(
            Merchant merchant,
            PaymentTransaction paymentTransaction,
            String entryType,
            BigDecimal amount
    ) {
        this.merchant = merchant;
        this.paymentTransaction = paymentTransaction;
        this.entryType = entryType;
        this.amount = amount;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public String getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
