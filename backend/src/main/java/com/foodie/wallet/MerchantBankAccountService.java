
package com.foodie.wallet;

import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class MerchantBankAccountService {

    private final MerchantBankAccountRepository accountRepository;
    private final MerchantRepository merchantRepository;
    private final byte[] encryptionKey;
    private final byte[] fingerprintKey;
    private final SecureRandom random = new SecureRandom();

    public MerchantBankAccountService(
            MerchantBankAccountRepository accountRepository,
            MerchantRepository merchantRepository,
            @Value("${foodie.bank.encryption-key}") String encryptionKey,
            @Value("${foodie.bank.fingerprint-key}") String fingerprintKey) {

        this.accountRepository = accountRepository;
        this.merchantRepository = merchantRepository;

        this.encryptionKey = Base64.getDecoder().decode(encryptionKey);
        this.fingerprintKey = Base64.getDecoder().decode(fingerprintKey);

        if (this.encryptionKey.length != 32 ||
                this.fingerprintKey.length != 32) {
            throw new IllegalArgumentException(
                    "Bank security keys must each be 32 bytes."
            );
        }
    }

    private Merchant getMerchant(String email) {
        return merchantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Merchant not found."));
    }

    @Transactional
    public MerchantBankAccount addAccount(
            String email,
            String bank,
            String holder,
            String number,
            String branch) {

        Merchant merchant = getMerchant(email);

        if (bank == null || holder == null ||
                number == null || branch == null) {
            throw new IllegalArgumentException("All fields are required.");
        }

        bank = bank.trim();
        holder = holder.trim();
        branch = branch.trim();

        String cleanNumber = number.replaceAll("\\s+", "");

        if (bank.isEmpty() || holder.isEmpty() || branch.isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }

        if (!cleanNumber.matches("[0-9]{4,20}")) {
            throw new IllegalArgumentException(
                    "Invalid bank account number.");
        }

        String fingerprint = fingerprint(bank, cleanNumber);

        if (accountRepository.existsByBankNameAndAccountFingerprint(
                bank, fingerprint)) {
            throw new IllegalArgumentException(
                    "This bank account is already registered.");
        }

        MerchantBankAccount account = new MerchantBankAccount();

        account.setMerchant(merchant);
        account.setBankName(bank);
        account.setAccountHolder(holder);
        account.setBranch(branch);
        account.setAccountNumberEncrypted(encrypt(cleanNumber));
        account.setAccountFingerprint(fingerprint);
        account.setLastFour(cleanNumber.substring(cleanNumber.length() - 4));

        return accountRepository.saveAndFlush(account);
    }

    @Transactional(readOnly = true)
    public List<MerchantBankAccount> getAccounts(String email) {
        Merchant merchant = getMerchant(email);
        return accountRepository.findByMerchant_IdOrderByIdDesc(
                merchant.getId());
    }

    @Transactional
    public void removeAccount(String email, Long accountId) {
        Merchant merchant = getMerchant(email);

        MerchantBankAccount account = accountRepository
                .findByIdAndMerchant_Id(accountId, merchant.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bank account not found."));

        accountRepository.delete(account);
    }

    private String encrypt(String accountNumber) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(encryptionKey, "AES"),
                    new GCMParameterSpec(128, iv)
            );

            byte[] encrypted = cipher.doFinal(
                    accountNumber.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[iv.length + encrypted.length];

            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length,
                    encrypted.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to protect bank account details.", e);
        }
    }

    private String fingerprint(String bank, String accountNumber) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(fingerprintKey, "HmacSHA256"));

            String normalizedBank = bank.trim().toLowerCase(
                    java.util.Locale.ROOT);

            String value = normalizedBank + ":" + accountNumber;

            byte[] hash = mac.doFinal(
                    value.getBytes(StandardCharsets.UTF_8));

            return java.util.HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to validate bank account.", e);
        }
    }
}
