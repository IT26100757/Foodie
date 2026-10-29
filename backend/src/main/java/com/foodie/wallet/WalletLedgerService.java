
package com.foodie.wallet;

import com.foodie.model.Merchant;
import com.foodie.model.PaymentTransaction;
import com.foodie.repository.MerchantRepository;
import com.foodie.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class WalletLedgerService {

    private static final BigDecimal COMMISSION_RATE =
            new BigDecimal("0.10");

    private final WalletLedgerRepository ledgerRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final MerchantRepository merchantRepository;

    public WalletLedgerService(
            WalletLedgerRepository ledgerRepository,
            PaymentTransactionRepository paymentRepository,
            MerchantRepository merchantRepository
    ) {
        this.ledgerRepository = ledgerRepository;
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public void recordVerifiedEarning(Long paymentId) {

        PaymentTransaction payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        if (!"COMPLETED".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalStateException(
                    "Only completed payments can create earnings");
        }

        if (payment.getOrder() == null
                || payment.getOrder().getRestaurant() == null
                || payment.getOrder().getRestaurant().getMerchant() == null) {
            throw new IllegalStateException(
                    "Payment has no valid merchant");
        }

        Long merchantId = payment.getOrder()
                .getRestaurant()
                .getMerchant()
                .getId();

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new IllegalStateException("Merchant not found"));

        BigDecimal gross = payment.getAmount();

        if (gross == null || gross.signum() <= 0) {
            throw new IllegalStateException("Invalid payment amount");
        }

        gross = gross.setScale(2, RoundingMode.HALF_UP);

        BigDecimal commission = gross
                .multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal net = gross.subtract(commission);

        saveOnce(merchant, payment, "EARNING", gross);
        saveOnce(merchant, payment, "COMMISSION", commission.negate());
        saveOnce(merchant, payment, "NET_EARNING", net);
    }

    private void saveOnce(
            Merchant merchant,
            PaymentTransaction payment,
            String entryType,
            BigDecimal amount
    ) {
        if (ledgerRepository.existsByPaymentTransactionIdAndEntryType(
                payment.getId(), entryType)) {
            return;
        }

        ledgerRepository.save(
                new WalletLedgerEntry(
                        merchant,
                        payment,
                        entryType,
                        amount
                )
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal getLedgerTotal(Long merchantId) {

        return ledgerRepository
                .findByMerchantIdOrderByCreatedAtDesc(merchantId)
                .stream()
                .filter(entry ->
                        "NET_EARNING".equals(entry.getEntryType()))
                .map(WalletLedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
