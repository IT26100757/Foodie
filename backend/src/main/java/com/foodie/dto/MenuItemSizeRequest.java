
package com.foodie.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class MenuItemSizeRequest {

    @NotBlank(message = "Size name is required")
    private String sizeName;

    @NotNull(message = "Size price is required")
    @DecimalMin(value = "0.00", message = "Size price cannot be negative")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    public MenuItemSizeRequest() {
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
