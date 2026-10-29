
package com.foodie.wallet;

import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class WalletTransactionService {

    private final WalletTransactionRepository repository;
    private final MerchantRepository merchantRepository;

    public WalletTransactionService(
            WalletTransactionRepository repository,
            MerchantRepository merchantRepository) {
        this.repository = repository;
        this.merchantRepository = merchantRepository;
    }

    private Merchant getMerchant(String email) {
        return merchantRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Merchant not found."
                ));
    }

    @Transactional
    public WalletTransaction start(
            String merchantEmail,
            CreateWalletTransferRequest request) {

        Merchant merchant = getMerchant(merchantEmail);

        String transactionId = "FDW-" +
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .toUpperCase();

        WalletTransaction transaction = new WalletTransaction(
                transactionId,
                request.bankName().trim(),
                request.accountHolder().trim(),
                request.accountLastFour(),
                request.amount()
        );

        transaction.setMerchant(merchant);

        return repository.saveAndFlush(transaction);
    }

    @Transactional
    public WalletTransaction markUnavailable(
            String merchantEmail,
            String transactionId) {

        Merchant merchant = getMerchant(merchantEmail);

        WalletTransaction transaction = repository
                .findByTransactionIdAndMerchant_Id(
                        transactionId,
                        merchant.getId()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Transaction not found."
                ));

        transaction.setStatus(WalletTransferStatus.FAILED);
        transaction.setFailureReason(
                "Bank transfer processing is not configured."
        );

        return repository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> findAll(String merchantEmail) {

        Merchant merchant = getMerchant(merchantEmail);

        return repository.findByMerchant_IdOrderByCreatedAtDesc(
                merchant.getId()
        );
    }

    @Transactional(readOnly = true)
    public WalletTransaction find(
            String merchantEmail,
            String transactionId) {

        Merchant merchant = getMerchant(merchantEmail);

        return repository.findByTransactionIdAndMerchant_Id(
                transactionId,
                merchant.getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Transaction not found."
        ));
    }
}
