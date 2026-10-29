
package com.foodie.dto;

import java.math.BigDecimal;

public class TopSellingItemResponse {

    private final Long menuItemId;
    private final String itemName;
    private final String imageUrl;
    private int quantitySold;
    private BigDecimal revenue;

    public TopSellingItemResponse(
            Long menuItemId,
            String itemName,
            String imageUrl,
            int quantitySold,
            BigDecimal revenue) {

        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.imageUrl = imageUrl;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    public void addSale(int quantity, BigDecimal subtotal) {
        this.quantitySold += quantity;
        this.revenue = this.revenue.add(subtotal);
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
