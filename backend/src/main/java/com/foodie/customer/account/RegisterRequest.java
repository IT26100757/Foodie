package com.foodie.customer.account;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "First name is required.")
        @Size(max = 100, message = "First name must not exceed 100 characters.")
        String firstName,

        @NotBlank(message = "Last name is required.")
        @Size(max = 100, message = "Last name must not exceed 100 characters.")
        String lastName,

        @NotBlank(message = "NIC is required.")
        @Pattern(
                regexp = "^(?:[0-9]{9}[vVxX]|[0-9]{12})$",
                message = "Enter a valid Sri Lankan NIC."
        )
        String nationalId,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 254, message = "Email must not exceed 254 characters.")
        String email,

        @NotBlank(message = "Phone number is required.")
        @Pattern(
                regexp = "^\\+94[0-9]{9}$",
                message = "Phone number must contain +94 followed by 9 digits."
        )
        String phone,

        @NotBlank(message = "Address line 1 is required.")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters.")
        String address1,

        @NotBlank(message = "Address line 2 is required.")
        @Size(max = 255, message = "Address line 2 must not exceed 255 characters.")
        String address2,

        @NotBlank(message = "Address line 3 is required.")
        @Size(max = 255, message = "Address line 3 must not exceed 255 characters.")
        String address3,

        @NotBlank(message = "Password is required.")
        @Size(
                min = 8,
                max = 128,
                message = "Password must contain between 8 and 128 characters."
        )
        String password,

        @NotBlank(message = "Please confirm your password.")
        @Size(max = 128, message = "Confirmation must not exceed 128 characters.")
        String confirmPassword,

        @NotNull(message = "Please accept the terms and privacy policy.")
        @AssertTrue(message = "Please accept the terms and privacy policy.")
        Boolean terms

) {

    @Override
    public String toString() {
        return "RegisterRequest[personal details and passwords hidden]";
    }
}