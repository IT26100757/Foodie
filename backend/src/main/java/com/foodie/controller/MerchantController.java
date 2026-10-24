
package com.foodie.controller;

import com.foodie.dto.MerchantRegistrationRequest;
import com.foodie.service.MerchantService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerMerchant(
            @Valid @RequestBody MerchantRegistrationRequest request) {

        Long merchantId = merchantService.registerMerchant(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Merchant registration successful. Awaiting approval.",
                        "merchantId", merchantId
                )
        );
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleError(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(
                Map.of("message", ex.getMessage())
        );
    }

}
