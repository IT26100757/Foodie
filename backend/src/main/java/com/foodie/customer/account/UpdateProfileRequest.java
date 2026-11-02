package com.foodie.customer.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "First name is required.")
        @Size(max = 100, message = "First name must not exceed 100 characters.")
        String firstName,

        @NotBlank(message = "Last name is required.")
        @Size(max = 100, message = "Last name must not exceed 100 characters.")
        String lastName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Please enter a valid email address.")
        @Size(max = 254, message = "Email must not exceed 254 characters.")
        String email,

        @NotBlank(message = "Phone number is required.")
        @Pattern(
                regexp = "^\\+94[0-9]{9}$",
                message = "Phone number must start with +94 followed by 9 digits."
        )
        String phone

) {

    @Override
    public String toString() {
        return "UpdateProfileRequest[personal details hidden]";
    }
}