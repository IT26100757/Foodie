
package com.foodie.service;

import com.foodie.model.PaymentTransaction;
import com.foodie.repository.PaymentTransactionRepository;
import com.foodie.wallet.WalletLedgerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class DemoPaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final WalletLedgerService walletLedgerService;
    private final Environment environment;

    @Value("${foodie.payments.demo.enabled:false}")
    private boolean demoEnabled;

    public DemoPaymentService(
            PaymentTransactionRepository paymentRepository,
            WalletLedgerService walletLedgerService,
            Environment environment) {
        this.paymentRepository = paymentRepository;
        this.walletLedgerService = walletLedgerService;
        this.environment = environment;
    }

    private void verifyDemoMode() {
        if (!demoEnabled ||
                Arrays.stream(environment.getActiveProfiles())
                        .noneMatch("demo"::equals)) {
            throw new IllegalStateException(
                    "Demo payments are disabled");
        }
    }

    @Transactional
    public PaymentTransaction confirmDemoPayment(
            Long paymentId, Long merchantId) {

        verifyDemoMode();

        if (paymentId == null || merchantId == null) {
            throw new IllegalArgumentException(
                    "Payment and merchant IDs are required");
        }

        PaymentTransaction payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        Long ownerId = payment.getOrder()
                .getRestaurant()
                .getMerchant()
                .getId();

        if (!ownerId.equals(merchantId)) {
            throw new SecurityException(
                    "Payment does not belong to this merchant");
        }

        if ("CANCELLED".equalsIgnoreCase(
                payment.getOrder().getStatus())) {
            throw new IllegalStateException(
                    "Cannot confirm payment for a cancelled order");
        }

        if (!"PENDING".equals(payment.getStatus())) {
            throw new IllegalStateException(
                    "Only pending payments can be confirmed");
        }

        if (paymentRepository.existsByOrderIdAndStatus(
                payment.getOrder().getId(), "COMPLETED")) {
            throw new IllegalStateException(
                    "This order already has a completed payment");
        }

        if ("CASH_ON_DELIVERY".equals(payment.getPaymentMethod())
                && !"DELIVERED".equalsIgnoreCase(
                payment.getOrder().getStatus())) {
            throw new IllegalStateException(
                    "COD payment requires a delivered order");
        }

        if (!"CARD".equals(payment.getPaymentMethod())
                && !"CASH_ON_DELIVERY".equals(
                payment.getPaymentMethod())) {
            throw new IllegalStateException(
                    "Unsupported payment method");
        }

        payment.setStatus("COMPLETED");
        payment.setProvider("FOODIE_DEMO");

        PaymentTransaction saved = paymentRepository.save(payment);

        walletLedgerService.recordVerifiedEarning(saved.getId());

        return saved;
    }
}
