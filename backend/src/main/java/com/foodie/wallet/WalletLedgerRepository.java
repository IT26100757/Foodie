
package com.foodie.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface WalletLedgerRepository
        extends JpaRepository<WalletLedgerEntry, Long> {

    List<WalletLedgerEntry>
    findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    boolean existsByPaymentTransactionIdAndEntryType(
            Long paymentTransactionId,
            String entryType
    );

    @Query("""
        SELECT SUM(e.amount)
        FROM WalletLedgerEntry e
        WHERE e.merchant.id = :merchantId
        """)
    BigDecimal calculateBalance(
            @Param("merchantId") Long merchantId
    );
}
