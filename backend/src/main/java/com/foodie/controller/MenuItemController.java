package com.foodie.controller;

import com.foodie.dto.MenuItemRequest;
import com.foodie.dto.MenuItemResponse;
import com.foodie.model.MenuItem;
import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import com.foodie.service.MenuItemService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/merchant/restaurants/{restaurantId}/menu")
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final MerchantRepository merchantRepository;

    public MenuItemController(
            MenuItemService menuItemService,
            MerchantRepository merchantRepository) {

        this.menuItemService = menuItemService;
        this.merchantRepository = merchantRepository;
    }

    @GetMapping
    public List<MenuItemResponse> getMenuItems(
            @PathVariable Long restaurantId,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return menuItemService.getMenuItems(merchantId, restaurantId)
                .stream()
                .map(MenuItemResponse::new)
                .toList();
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        MenuItem item = toMenuItem(request);

        MenuItem saved = menuItemService.addMenuItem(
                merchantId, restaurantId, item);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MenuItemResponse(saved));
    }

    @PutMapping("/{itemId}")
    public MenuItemResponse updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        MenuItem updated = menuItemService.updateMenuItem(
                merchantId,
                restaurantId,
                itemId,
                toMenuItem(request));

        return new MenuItemResponse(updated);
    }

    @PatchMapping("/{itemId}/availability")
    public MenuItemResponse updateAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        Boolean available = request.get("available");

        if (available == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Available field is required");
        }

        MenuItem updated = menuItemService.updateAvailability(
                merchantId, restaurantId, itemId, available);

        return new MenuItemResponse(updated);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        menuItemService.deleteMenuItem(
                merchantId, restaurantId, itemId);

        return ResponseEntity.noContent().build();
    }

    private Long getMerchantId(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream()
                        .noneMatch(a ->
                                a.getAuthority().equals("ROLE_MERCHANT"))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Merchant access required");
        }

        String email = authentication.getName();

        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Merchant account not found"));

        return merchant.getId();
    }

    private MenuItem toMenuItem(MenuItemRequest request) {

        MenuItem item = new MenuItem();

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setImageUrl(request.getImageUrl());
        item.setAvailable(request.isAvailable());

        return item;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            NoSuchElementException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }
}