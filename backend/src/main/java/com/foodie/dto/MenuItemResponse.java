package com.foodie.dto;

import com.foodie.model.MenuItem;
import com.foodie.model.MenuItemSize;

import java.math.BigDecimal;
import java.util.List;

public class MenuItemResponse {

    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private boolean available;

    // New size variation fields
    private boolean sizesEnabled;
    private List<MenuItemSizeResponse> sizes;

    public MenuItemResponse(MenuItem item) {
        this.id = item.getId();
        this.restaurantId = item.getRestaurant().getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.price = item.getPrice();
        this.category = item.getCategory();
        this.imageUrl = item.getImageUrl();
        this.available = item.isAvailable();

        this.sizes = item.getSizes()
                .stream()
                .map(MenuItemSizeResponse::new)
                .toList();

        this.sizesEnabled = !this.sizes.isEmpty();
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isSizesEnabled() {
        return sizesEnabled;
    }

    public List<MenuItemSizeResponse> getSizes() {
        return sizes;
    }

    public static class MenuItemSizeResponse {

        private final Long id;
        private final String sizeName;
        private final BigDecimal price;

        public MenuItemSizeResponse(MenuItemSize size) {
            this.id = size.getId();
            this.sizeName = size.getSizeName();
            this.price = size.getPrice();
        }

        public Long getId() {
            return id;
        }

        public String getSizeName() {
            return sizeName;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }
}
