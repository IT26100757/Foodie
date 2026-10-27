
package com.foodie.dto;

import java.math.BigDecimal;
import java.util.List;

public record MerchantPerformanceResponse(
        int days,
        BigDecimal totalSales,
        long totalOrders,
        BigDecimal averageOrderValue,
        List<DailyPerformanceResponse> dailyPerformance
) {
}
