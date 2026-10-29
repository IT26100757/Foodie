
package com.foodie.service;

import com.foodie.model.Order;
import com.foodie.model.PaymentTransaction;
import com.foodie.repository.OrderRepository;
import com.foodie.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentTransactionService(
            PaymentTransactionRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentTransaction createPendingPayment(
            Long orderId,
            String paymentMethod
    ) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }

        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        String method = paymentMethod.trim().toUpperCase(Locale.ROOT);

        if (!method.equals("CARD") &&
                !method.equals("CASH_ON_DELIVERY")) {
            throw new IllegalArgumentException("Unsupported payment method");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found")
                );

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException(
                    "Cannot create payment for a cancelled order"
            );
        }


        if (paymentRepository.existsByOrderIdAndStatus(
                orderId, "COMPLETED")) {
            throw new IllegalStateException(
                    "This order already has a completed payment");
        }


        BigDecimal amount = order.getTotalAmount();

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException("Invalid order amount");
        }


        PaymentTransaction existingPayment = paymentRepository
                .findFirstByOrderIdAndPaymentMethodAndStatusOrderByCreatedAtDesc(
                        orderId,
                        method,
                        "PENDING"
                )
                .orElse(null);

        if (existingPayment != null) {
            return existingPayment;
        }

        PaymentTransaction payment = new PaymentTransaction();
        payment.setOrder(order);
        payment.setPaymentMethod(method);
        payment.setAmount(amount);
        payment.setStatus("PENDING");

        return paymentRepository.save(payment);

    }
}
