
package com.foodie.repository;

import com.foodie.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByOrderIdOrderByCreatedAtDesc(
            Long orderId
    );

    Optional<PaymentTransaction> findByProviderAndProviderTransactionId(
            String provider,
            String providerTransactionId
    );

    // Find an existing pending payment for an order
    Optional<PaymentTransaction>
    findFirstByOrderIdAndPaymentMethodAndStatusOrderByCreatedAtDesc(
            Long orderId,
            String paymentMethod,
            String status
    );


    List<PaymentTransaction>
    findByOrderRestaurantMerchantIdOrderByCreatedAtDesc(
            Long merchantId
    );


    boolean existsByOrderIdAndStatus(
            Long orderId,
            String status
    );
}
