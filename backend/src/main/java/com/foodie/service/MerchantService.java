
package com.foodie.service;

import com.foodie.dto.MerchantRegistrationRequest;
import com.foodie.model.Merchant;
import com.foodie.model.Restaurant;
import com.foodie.repository.MerchantRepository;
import com.foodie.repository.RestaurantRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    public MerchantService(
            MerchantRepository merchantRepository,
            RestaurantRepository restaurantRepository,
            PasswordEncoder passwordEncoder) {

        this.merchantRepository = merchantRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long registerMerchant(MerchantRegistrationRequest request) {

        // Check for duplicate email
        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check for duplicate national ID
        if (merchantRepository.existsByNationalId(request.getNationalId())) {
            throw new IllegalArgumentException("National ID already registered");
        }

        // Create merchant
        Merchant merchant = new Merchant();

        merchant.setFirstName(request.getFirstName());
        merchant.setLastName(request.getLastName());
        merchant.setNationalId(request.getNationalId());
        merchant.setEmail(request.getEmail());

        // Hash password before saving
        merchant.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        merchant.setVerificationStatus(
                Merchant.VerificationStatus.PENDING
        );

        // Save merchant first
        Merchant savedMerchant = merchantRepository.save(merchant);

        // Create restaurant
        Restaurant restaurant = new Restaurant();

        restaurant.setRestaurantName(request.getRestaurantName());
        restaurant.setRestaurantAddress(request.getRestaurantAddress());
        String phone = request.getRestaurantPhone();

        if (phone.startsWith("0")) {
            phone = "+94" + phone.substring(1);
        }

        restaurant.setRestaurantPhone(phone);

        // Link restaurant to merchant
        restaurant.setMerchant(savedMerchant);

        // Save restaurant
        restaurantRepository.save(restaurant);

        return savedMerchant.getId();
    }
}
