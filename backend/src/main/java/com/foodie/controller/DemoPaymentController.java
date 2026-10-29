
package com.foodie.controller;

import com.foodie.model.Merchant;
import com.foodie.model.PaymentTransaction;
import com.foodie.repository.MerchantRepository;
import com.foodie.service.DemoPaymentService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Profile("demo")
@RequestMapping("/api/merchant/demo/payments")
public class DemoPaymentController {

    private final DemoPaymentService demoPaymentService;
    private final MerchantRepository merchantRepository;

    public DemoPaymentController(
            DemoPaymentService demoPaymentService,
            MerchantRepository merchantRepository) {
        this.demoPaymentService = demoPaymentService;
        this.merchantRepository = merchantRepository;
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_MERCHANT"))) {
            return ResponseEntity.status(403).build();
        }

        Merchant merchant = merchantRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Merchant not found"));

        PaymentTransaction payment =
                demoPaymentService.confirmDemoPayment(
                        paymentId, merchant.getId());

        return ResponseEntity.ok(Map.of(
                "paymentId", payment.getId(),
                "orderId", payment.getOrder().getId(),
                "status", payment.getStatus(),
                "provider", payment.getProvider(),
                "message", "SIMULATED PAYMENT - NO REAL MONEY"
        ));
    }
}
