
package com.foodie.wallet;

import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/wallet/earnings")
public class WalletEarningsController {

    private final WalletLedgerRepository ledgerRepository;
    private final MerchantRepository merchantRepository;

    public WalletEarningsController(
            WalletLedgerRepository ledgerRepository,
            MerchantRepository merchantRepository) {
        this.ledgerRepository = ledgerRepository;
        this.merchantRepository = merchantRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public WalletEarningsResponse getEarnings(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream()
                        .noneMatch(a ->
                                "ROLE_MERCHANT".equals(a.getAuthority()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Merchant access required");
        }

        Merchant merchant = merchantRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Merchant not found"));

        List<WalletEarningItem> transactions =
                ledgerRepository
                        .findByMerchantIdOrderByCreatedAtDesc(
                                merchant.getId())
                        .stream()
                        .filter(entry ->
                                "EARNING".equals(entry.getEntryType()))
                        .map(entry -> new WalletEarningItem(
                                entry.getId(),
                                entry.getPaymentTransaction().getId(),
                                entry.getPaymentTransaction()
                                        .getOrder().getId(),
                                entry.getAmount(),
                                entry.getCurrency(),
                                entry.getCreatedAt()
                        ))
                        .toList();

        BigDecimal total = transactions.stream()
                .map(WalletEarningItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new WalletEarningsResponse(
                total,
                "LKR",
                transactions.size(),
                transactions
        );
    }

    public record WalletEarningItem(
            Long id,
            Long paymentId,
            Long orderId,
            BigDecimal amount,
            String currency,
            Instant createdAt
    ) {}

    public record WalletEarningsResponse(
            BigDecimal recordedEarnings,
            String currency,
            int transactionCount,
            List<WalletEarningItem> transactions
    ) {}
}
