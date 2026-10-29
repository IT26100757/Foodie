
package com.foodie.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyPerformanceResponse(
        LocalDate date,
        BigDecimal sales,
        long orders
) {
}
