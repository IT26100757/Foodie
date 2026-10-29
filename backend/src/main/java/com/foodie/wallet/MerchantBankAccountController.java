
package com.foodie.wallet;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet/bank-accounts")
public class MerchantBankAccountController {

    private final MerchantBankAccountService service;

    public MerchantBankAccountController(
            MerchantBankAccountService service) {
        this.service = service;
    }

    // Add a new bank account
    @PostMapping
    public ResponseEntity<BankAccountResponse> add(
            @Valid @RequestBody CreateBankAccountRequest request,
            Authentication authentication) {

        MerchantBankAccount account = service.addAccount(
                authentication.getName(),
                request.bank(),
                request.name(),
                request.number(),
                request.branch()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BankAccountResponse.from(account));
    }

    // Get accounts of the logged-in merchant
    @GetMapping
    public List<BankAccountResponse> list(
            Authentication authentication) {

        return service.getAccounts(authentication.getName())
                .stream()
                .map(BankAccountResponse::from)
                .toList();
    }

    // Remove a bank account
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(
            @PathVariable Long id,
            Authentication authentication) {

        service.removeAccount(
                authentication.getName(),
                id
        );

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(
                Map.of("message", ex.getMessage())
        );
    }
}
