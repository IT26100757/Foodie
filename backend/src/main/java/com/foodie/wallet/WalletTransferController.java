
package com.foodie.wallet;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet/transfers")
public class WalletTransferController {

    private final WalletTransactionService service;

    public WalletTransferController(WalletTransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WalletTransferResponse> create(
            @Valid @RequestBody CreateWalletTransferRequest request,
            Authentication authentication) {

        String merchantEmail = authentication.getName();

        WalletTransaction transaction =
                service.start(merchantEmail, request);

        transaction = service.markUnavailable(
                merchantEmail,
                transaction.getTransactionId()
        );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(WalletTransferResponse.from(transaction));
    }

    @GetMapping
    public List<WalletTransferResponse> list(
            Authentication authentication) {

        return service.findAll(authentication.getName())
                .stream()
                .map(WalletTransferResponse::from)
                .toList();
    }

    @GetMapping("/{transactionId}")
    public WalletTransferResponse get(
            @PathVariable String transactionId,
            Authentication authentication) {

        return WalletTransferResponse.from(
                service.find(
                        authentication.getName(),
                        transactionId
                )
        );
    }
}
