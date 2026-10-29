
package com.foodie.wallet;

import jakarta.validation.constraints.NotBlank;

public record CreateBankAccountRequest(
        @NotBlank String bank,
        @NotBlank String name,
        @NotBlank String number,
        @NotBlank String branch
) {
}
