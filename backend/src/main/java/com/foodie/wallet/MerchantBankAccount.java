
package com.foodie.wallet;

import com.foodie.model.Merchant;
import jakarta.persistence.*;

@Entity
@Table(
        name = "merchant_bank_accounts",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"bank_name", "account_fingerprint"}
        )
)
public class MerchantBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;

    @Column(nullable = false)
    private String branch;

    @Column(name = "account_number_encrypted",
            nullable = false, length = 512)
    private String accountNumberEncrypted;

    @Column(name = "account_fingerprint",
            nullable = false, length = 64)
    private String accountFingerprint;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    public MerchantBankAccount() {}

    public Long getId() { return id; }

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getBranch() { return branch; }
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getAccountNumberEncrypted() {
        return accountNumberEncrypted;
    }
    public void setAccountNumberEncrypted(String value) {
        this.accountNumberEncrypted = value;
    }

    public String getAccountFingerprint() {
        return accountFingerprint;
    }
    public void setAccountFingerprint(String value) {
        this.accountFingerprint = value;
    }

    public String getLastFour() { return lastFour; }
    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }
}
