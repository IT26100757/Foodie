package com.foodie.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    java.util.List<WalletTransaction> findAllByOrderByCreatedAtDesc();
}
