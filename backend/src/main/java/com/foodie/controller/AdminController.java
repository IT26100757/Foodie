package com.foodie.controller;

import com.foodie.dto.PendingMerchantResponse;
import com.foodie.service.AdminMerchantService;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminMerchantService adminMerchantService;

    public AdminController(AdminMerchantService adminMerchantService) {
        this.adminMerchantService = adminMerchantService;
    }

    @GetMapping("/test")
    public Map<String, String> testAdmin(Authentication authentication) {
        return Map.of(
                "message", "Platform Admin authentication successful",
                "role", authentication.getAuthorities().toString()
        );
    }

    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(CsrfToken csrfToken) {

        return Map.of(
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName(),
                "token", csrfToken.getToken()
        );
    }

    @GetMapping("/merchants/pending")
    public List<PendingMerchantResponse> getPendingMerchants() {
        return adminMerchantService.getPendingMerchants();
    }

    @PostMapping("/merchants/{merchantId}/approve")
    public ResponseEntity<Map<String, String>> approveMerchant(
            @PathVariable Long merchantId) {

        adminMerchantService.approveMerchant(merchantId);

        return ResponseEntity.ok(
                Map.of("message", "Merchant approved successfully")
        );
    }

    @PostMapping("/merchants/{merchantId}/reject")
    public ResponseEntity<Map<String, String>> rejectMerchant(
            @PathVariable Long merchantId) {

        adminMerchantService.rejectMerchant(merchantId);

        return ResponseEntity.ok(
                Map.of("message", "Merchant rejected successfully")
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleMerchantNotFound(
            IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMerchantStatus(
            IllegalStateException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }
}