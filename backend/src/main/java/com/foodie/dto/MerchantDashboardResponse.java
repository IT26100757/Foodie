
package com.foodie.dto;

import java.math.BigDecimal;

public record MerchantDashboardResponse(
        long totalOrders,
        long newOrders,
        long preparingOrders,
        long readyOrders,
        long onDeliveryOrders,
        long deliveredOrders,
        long cancelledOrders,
        BigDecimal deliveredRevenue
) {
}
