package com.foodie.dto;

import com.foodie.model.MenuItem;
import java.math.BigDecimal;

public class MenuItemResponse {

    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private boolean available;

    public MenuItemResponse(MenuItem item) {
        this.id = item.getId();
        this.restaurantId = item.getRestaurant().getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.price = item.getPrice();
        this.category = item.getCategory();
        this.imageUrl = item.getImageUrl();
        this.available = item.isAvailable();
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
}