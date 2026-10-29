
package com.foodie.controller;

import com.foodie.model.Merchant;
import com.foodie.model.Order;
import com.foodie.model.PaymentTransaction;
import com.foodie.repository.MerchantRepository;
import com.foodie.service.OrderService;
import com.foodie.service.PaymentTransactionService;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@Profile("demo")
@RequestMapping("/api/merchant/demo")
public class DemoPaymentCreationController {

    private final MerchantRepository merchantRepository;
    private final OrderService orderService;
    private final PaymentTransactionService paymentService;

    public DemoPaymentCreationController(
            MerchantRepository merchantRepository,
            OrderService orderService,
            PaymentTransactionService paymentService) {
        this.merchantRepository = merchantRepository;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping("/restaurants/{restaurantId}/orders/{orderId}/payments")
    public Map<String, Object> createPayment(
            @PathVariable Long restaurantId,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream()
                        .noneMatch(a -> "ROLE_MERCHANT"
                                .equals(a.getAuthority()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Merchant access required");
        }

        Merchant merchant = merchantRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Merchant not found"));

        // Verify merchant owns the restaurant and order.
        Order order = orderService.getOrder(
                merchant.getId(), restaurantId, orderId);

        String method = body.get("paymentMethod");

        if (method == null ||
                (!"CARD".equals(method) &&
                        !"CASH_ON_DELIVERY".equals(method))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment method");
        }

        PaymentTransaction payment =
                paymentService.createPendingPayment(
                        order.getId(), method);

        return Map.of(
                "paymentId", payment.getId(),
                "orderId", order.getId(),
                "paymentMethod", payment.getPaymentMethod(),
                "amount", payment.getAmount(),
                "status", payment.getStatus()
        );
    }
}
