package com.foodie.customer.account;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/customer/account/profile")
public class CustomerProfileController {

    private final CustomerProfileService profileService;

    public CustomerProfileController(
            CustomerProfileService profileService
    ) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<CustomerAccountResponse> getProfile(
            @AuthenticationPrincipal CustomerPrincipal principal
    ) {
        Long customerId = requireCustomerId(principal);

        CustomerAccountResponse profile =
                profileService.getProfile(customerId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(profile);
    }

    @PutMapping
    public ResponseEntity<CustomerAccountResponse> updateProfile(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long customerId = requireCustomerId(principal);

        CustomerAccountResponse profile =
                profileService.updateProfile(customerId, request);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(profile);
    }

    private Long requireCustomerId(CustomerPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in."
            );
        }

        return principal.getCustomerId();
    }
}