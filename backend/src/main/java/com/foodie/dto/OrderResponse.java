
package com.foodie.dto;

import com.foodie.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponse {

    private final Long id;
    private final Long restaurantId;
    private final String customerName;
    private final String customerPhone;
    private final String deliveryAddress;
    private final String orderType;
    private final String status;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final String paymentType;
    private BigDecimal additionalCharge;
    private String additionalChargeStatus;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.restaurantId = order.getRestaurant().getId();
        this.customerName = order.getCustomerName();
        this.customerPhone = order.getCustomerPhone();
        this.deliveryAddress = order.getDeliveryAddress();
        this.orderType = order.getOrderType();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.createdAt = order.getCreatedAt();
        this.paymentType = order.getPaymentMethod();
        this.additionalCharge = order.getAdditionalCharge();
        this.additionalChargeStatus = order.getAdditionalChargeStatus();
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public BigDecimal getAdditionalCharge() {
        return additionalCharge;
    }

    public void setAdditionalCharge(BigDecimal additionalCharge) {
        this.additionalCharge = additionalCharge;
    }

    public String getAdditionalChargeStatus() {
        return additionalChargeStatus;
    }

    public void setAdditionalChargeStatus(String additionalChargeStatus) {
        this.additionalChargeStatus = additionalChargeStatus;
    }
}
