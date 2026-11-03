package com.foodie.customer.account;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customer/account/addresses")
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    public CustomerAddressController(
            CustomerAddressService addressService
    ) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerAddressResponse>> listAddresses(
            @AuthenticationPrincipal CustomerPrincipal principal
    ) {
        Long customerId = requireCustomerId(principal);

        List<CustomerAddressResponse> addresses =
                addressService.listAddresses(customerId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(addresses);
    }

    @PostMapping
    public ResponseEntity<CustomerAddressResponse> createAddress(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody CustomerAddressRequest request
    ) {
        Long customerId = requireCustomerId(principal);

        CustomerAddressResponse address =
                addressService.createAddress(customerId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(address);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<CustomerAddressResponse> updateAddress(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @PathVariable("addressId") Long addressId,
            @Valid @RequestBody CustomerAddressRequest request
    ) {
        Long customerId = requireCustomerId(principal);

        CustomerAddressResponse address =
                addressService.updateAddress(
                        customerId,
                        addressId,
                        request
                );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(address);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @PathVariable("addressId") Long addressId
    ) {
        Long customerId = requireCustomerId(principal);

        addressService.deleteAddress(customerId, addressId);

        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
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