
package com.foodie.dto;

import com.foodie.model.OrderItem;
import java.math.BigDecimal;

public class OrderItemResponse {

    private final Long id;
    private final Long orderId;
    private final Long menuItemId;
    private final String itemName;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public OrderItemResponse(OrderItem item) {
        this.id = item.getId();
        this.orderId = item.getOrder().getId();
        this.menuItemId = item.getMenuItem() != null
                ? item.getMenuItem().getId()
                : null;
        this.itemName = item.getItemName();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.subtotal = item.getSubtotal();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
