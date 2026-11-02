package com.foodie.customer.account;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerAccountRepository
        extends JpaRepository<CustomerAccount, Long> {

    Optional<CustomerAccount> findByEmailIgnoreCase(String email);

    Optional<CustomerAccount> findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from CustomerAccount account where account.id = :customerId")
    Optional<CustomerAccount> findByIdForUpdate(
            @Param("customerId") Long customerId
    );
}