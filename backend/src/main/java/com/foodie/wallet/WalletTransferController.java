package com.foodie.wallet;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet/transfers")
public class WalletTransferController {
    private final WalletTransactionService service;

    public WalletTransferController(WalletTransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WalletTransferResponse> create(@Valid @RequestBody CreateWalletTransferRequest request) {
        WalletTransaction transaction = service.start(request);
        transaction = service.markUnavailable(transaction.getTransactionId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(WalletTransferResponse.from(transaction));
    }

    @GetMapping
    public java.util.List<WalletTransferResponse> list() {
        return service.findAll().stream().map(WalletTransferResponse::from).toList();
    }

    @GetMapping("/{transactionId}")
    public WalletTransferResponse get(@PathVariable String transactionId) {
        return WalletTransferResponse.from(service.find(transactionId));
    }
}
