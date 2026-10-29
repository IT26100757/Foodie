package com.foodie.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateWalletTransferRequest(
        @NotBlank @Size(max = 100) String bankName,
        @NotBlank @Size(max = 100) String accountHolder,
        @NotBlank @Pattern(regexp = "\\d{4}") String accountLastFour,
        @NotNull @DecimalMin("1.00") BigDecimal amount
) {
}
