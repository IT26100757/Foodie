
package com.foodie.repository;

import com.foodie.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    Optional<Merchant> findByEmail(String email);
}
