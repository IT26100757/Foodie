package com.foodie.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import jakarta.validation.Valid;

import java.math.BigDecimal;

public class MenuItemRequest {

    @NotBlank(message = "Menu item name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @Size(max = 255)
    private String category;

    @NotBlank(message = "Menu item image is required")
    @Size(max = 2048)
    private String imageUrl;

    private boolean available = true;

    private boolean sizesEnabled = false;

    @Valid
    private List<MenuItemSizeRequest> sizes;

    public MenuItemRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    public boolean isSizesEnabled() {
        return sizesEnabled;
    }

    public void setSizesEnabled(boolean sizesEnabled) {
        this.sizesEnabled = sizesEnabled;
    }

    public List<MenuItemSizeRequest> getSizes() {
        return sizes;
    }

    public void setSizes(List<MenuItemSizeRequest> sizes) {
        this.sizes = sizes;
    }
}