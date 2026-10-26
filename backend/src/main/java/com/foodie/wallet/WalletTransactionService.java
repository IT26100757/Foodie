package com.foodie.wallet;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WalletTransactionService {
    private final WalletTransactionRepository repository;

    public WalletTransactionService(WalletTransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WalletTransaction start(CreateWalletTransferRequest request) {
        String transactionId = "FDW-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        WalletTransaction transaction = new WalletTransaction(
                transactionId,
                request.bankName().trim(),
                request.accountHolder().trim(),
                request.accountLastFour(),
                request.amount()
        );
        return repository.saveAndFlush(transaction);
    }

    @Transactional
    public WalletTransaction markUnavailable(String transactionId) {
        WalletTransaction transaction = repository.findById(transactionId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));
        transaction.setStatus(WalletTransferStatus.FAILED);
        transaction.setFailureReason("Bank transfer processing is not configured.");
        return repository.save(transaction);
    }

    @Transactional(readOnly = true)
    public java.util.List<WalletTransaction> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public WalletTransaction find(String transactionId) {
        return repository.findById(transactionId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));
    }

}
