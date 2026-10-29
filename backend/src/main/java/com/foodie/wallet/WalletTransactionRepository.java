
package com.foodie.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, String> {

    List<WalletTransaction> findByMerchant_IdOrderByCreatedAtDesc(
            Long merchantId
    );

    Optional<WalletTransaction> findByTransactionIdAndMerchant_Id(
            String transactionId,
            Long merchantId
    );
}
