package com.foodie.service;

import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MerchantAuthService {

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;

    public MerchantAuthService(
            MerchantRepository merchantRepository,
            PasswordEncoder passwordEncoder) {

        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Merchant> verifyCredentials(
            String email, String password) {

        Optional<Merchant> merchantOptional =
                merchantRepository.findByEmail(email.trim());

        if (merchantOptional.isEmpty()) {
            return Optional.empty();
        }

        Merchant merchant = merchantOptional.get();

        if (!passwordEncoder.matches(
                password, merchant.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(merchant);
    }

    public boolean isApproved(Merchant merchant) {
        return merchant.getVerificationStatus()
                == Merchant.VerificationStatus.APPROVED;
    }
}