
package com.foodie.controller;

import com.foodie.dto.MerchantLoginRequest;
import com.foodie.dto.MerchantRegistrationRequest;
import com.foodie.model.Merchant;
import com.foodie.service.MerchantAuthService;
import com.foodie.service.MerchantService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;
    private final MerchantAuthService merchantAuthService;

    private final SecurityContextRepository contextRepository =
            new HttpSessionSecurityContextRepository();

    private final SessionAuthenticationStrategy sessionStrategy =
            new ChangeSessionIdAuthenticationStrategy();

    public MerchantController(
            MerchantService merchantService,
            MerchantAuthService merchantAuthService) {
        this.merchantService = merchantService;
        this.merchantAuthService = merchantAuthService;
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

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginMerchant(
            @Valid @RequestBody MerchantLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Optional<Merchant> result =
                merchantAuthService.verifyCredentials(
                        request.getEmail(),
                        request.getPassword()
                );

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "Invalid email or password"
                    ));
        }

        Merchant merchant = result.get();

        if (!merchantAuthService.isApproved(merchant)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message", "Your merchant account is not approved"
                    ));
        }

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        merchant.getEmail(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_MERCHANT")
                        )
                );

        sessionStrategy.onAuthentication(
                authentication,
                httpRequest,
                httpResponse
        );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        contextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "merchantId", merchant.getId(),
                "firstName", merchant.getFirstName(),
                "role", "MERCHANT"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleError(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(
                Map.of("message", ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid merchant details");

        return ResponseEntity.badRequest()
                .body(Map.of("message", message));
    }
}
