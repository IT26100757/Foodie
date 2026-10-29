
package com.foodie.controller;

import com.foodie.dto.MerchantPaymentResponse;
import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import com.foodie.repository.PaymentTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/payments")
public class MerchantPaymentController {

    private final MerchantRepository merchantRepository;
    private final PaymentTransactionRepository paymentRepository;

    public MerchantPaymentController(
            MerchantRepository merchantRepository,
            PaymentTransactionRepository paymentRepository
    ) {
        this.merchantRepository = merchantRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    public ResponseEntity<List<MerchantPaymentResponse>> getPayments(
            Authentication authentication
    ) {
        Merchant merchant = merchantRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Merchant not found")
                );

        List<MerchantPaymentResponse> payments = paymentRepository
                .findByOrderRestaurantMerchantIdOrderByCreatedAtDesc(
                        merchant.getId()
                )
                .stream()
                .map(payment -> new MerchantPaymentResponse(
                        payment.getId(),
                        payment.getOrder().getId(),
                        payment.getPaymentMethod(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getProvider(),
                        payment.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(payments);
    }
}
