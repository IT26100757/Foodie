package com.foodie.controller;

import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import com.foodie.repository.RestaurantRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/restaurants")
public class MerchantRestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final MerchantRepository merchantRepository;

    public MerchantRestaurantController(
            RestaurantRepository restaurantRepository,
            MerchantRepository merchantRepository) {
        this.restaurantRepository = restaurantRepository;
        this.merchantRepository = merchantRepository;
    }

    public record RestaurantSummary(
            Long id,
            String restaurantName,
            String restaurantAddress,
            String restaurantPhone
    ) {}

    @GetMapping
    public List<RestaurantSummary> getMyRestaurants(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream()
                        .noneMatch(a ->
                                "ROLE_MERCHANT".equals(a.getAuthority()))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Merchant access required"
            );
        }

        Merchant merchant = merchantRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Merchant not found"
                ));

        return restaurantRepository
                .findByMerchantId(merchant.getId())
                .stream()
                .map(restaurant -> new RestaurantSummary(
                        restaurant.getId(),
                        restaurant.getRestaurantName(),
                        restaurant.getRestaurantAddress(),
                        restaurant.getRestaurantPhone()
                ))
                .toList();
    }
}