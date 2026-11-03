package com.foodie.customer.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Email or phone number is required.")
        @Size(max = 254, message = "Email or phone number is too long.")
        String identifier,

        @NotBlank(message = "Password is required.")
        @Size(max = 128, message = "Password must not exceed 128 characters.")
        String password,

        boolean keepSignedIn

) {

    @Override
    public String toString() {
        return "LoginRequest[identifier and password hidden]";
    }
}