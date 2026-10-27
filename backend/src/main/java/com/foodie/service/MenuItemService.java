package com.foodie.service;

import com.foodie.model.MenuItem;
import com.foodie.model.Restaurant;
import com.foodie.repository.MenuItemRepository;
import com.foodie.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemService(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItems(
            Long merchantId, Long restaurantId) {

        verifyRestaurantOwnership(merchantId, restaurantId);

        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    @Transactional
    public MenuItem addMenuItem(
            Long merchantId, Long restaurantId, MenuItem item) {

        Restaurant restaurant =
                verifyRestaurantOwnership(merchantId, restaurantId);

        validateItem(item);

        MenuItem newItem = new MenuItem();
        newItem.setRestaurant(restaurant);
        copyDetails(item, newItem);

        return menuItemRepository.save(newItem);
    }

    @Transactional
    public MenuItem updateMenuItem(
            Long merchantId, Long restaurantId,
            Long itemId, MenuItem changes) {

        verifyRestaurantOwnership(merchantId, restaurantId);

        MenuItem existing = getOwnedItem(restaurantId, itemId);

        validateItem(changes);
        copyDetails(changes, existing);

        return menuItemRepository.save(existing);
    }

    @Transactional
    public MenuItem updateAvailability(
            Long merchantId, Long restaurantId,
            Long itemId, boolean available) {

        verifyRestaurantOwnership(merchantId, restaurantId);

        MenuItem item = getOwnedItem(restaurantId, itemId);
        item.setAvailable(available);

        return menuItemRepository.save(item);
    }

    @Transactional
    public void deleteMenuItem(
            Long merchantId, Long restaurantId, Long itemId) {

        verifyRestaurantOwnership(merchantId, restaurantId);

        MenuItem item = getOwnedItem(restaurantId, itemId);
        menuItemRepository.delete(item);
    }

    private Restaurant verifyRestaurantOwnership(
            Long merchantId, Long restaurantId) {

        return restaurantRepository.findByMerchantId(merchantId)
                .stream()
                .filter(r -> r.getId().equals(restaurantId))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Restaurant not found"));
    }

    private MenuItem getOwnedItem(
            Long restaurantId, Long itemId) {

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Menu item not found"));

        if (!item.getRestaurant().getId().equals(restaurantId)) {
            throw new NoSuchElementException("Menu item not found");
        }

        return item;
    }

    private void validateItem(MenuItem item) {

        if (item.getName() == null ||
                item.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Menu item name is required");
        }

        if (item.getPrice() == null ||
                item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Price must be zero or greater");
        }
    }

    private void copyDetails(MenuItem source, MenuItem target) {
        target.setName(source.getName().trim());
        target.setDescription(source.getDescription());
        target.setPrice(source.getPrice());
        target.setCategory(source.getCategory());
        target.setImageUrl(source.getImageUrl());
        target.setAvailable(source.isAvailable());
    }
}