
package com.foodie.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MerchantBankAccountRepository
        extends JpaRepository<MerchantBankAccount, Long> {

    // Get bank accounts belonging to one merchant
    List<MerchantBankAccount> findByMerchant_IdOrderByIdDesc(
            Long merchantId
    );

    // Check duplicate bank + account number fingerprint
    boolean existsByBankNameAndAccountFingerprint(
            String bankName,
            String accountFingerprint
    );

    // Find an account belonging to a specific merchant
    Optional<MerchantBankAccount> findByIdAndMerchant_Id(
            Long id,
            Long merchantId
    );
}
