package com.foodie.customer.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerAddressRequest(

        @NotBlank(message = "Address line 1 is required.")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters.")
        String address1,

        @NotBlank(message = "Address line 2 is required.")
        @Size(max = 255, message = "Address line 2 must not exceed 255 characters.")
        String address2,

        @NotBlank(message = "Address line 3 is required.")
        @Size(max = 255, message = "Address line 3 must not exceed 255 characters.")
        String address3,

        @Size(max = 100, message = "City must not exceed 100 characters.")
        String city,

        @Size(max = 20, message = "Postal code must not exceed 20 characters.")
        String postalCode,

        boolean defaultAddress
) {

    @Override
    public String toString() {
        return "CustomerAddressRequest[address details hidden]";
    }
}