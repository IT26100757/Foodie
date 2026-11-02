package com.foodie.customer.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/customer/auth")
public class CustomerAuthController {

    private final CustomerRegistrationService registrationService;
    private final CustomerAuthenticationService authenticationService;
    private final CustomerAccountRepository accountRepository;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public CustomerAuthController(
            CustomerRegistrationService registrationService,
            CustomerAuthenticationService authenticationService,
            CustomerAccountRepository accountRepository,
            @Qualifier("customerSecurityContextRepository")
            SecurityContextRepository securityContextRepository,
            @Qualifier("customerSessionAuthenticationStrategy")
            SessionAuthenticationStrategy sessionAuthenticationStrategy
    ) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.accountRepository = accountRepository;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfToken> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(csrfToken);
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerAccountResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        CustomerAccountResponse response =
                registrationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerAccountResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (loginRequest.keepSignedIn()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Keep signed in is not available yet."
            );
        }

        Authentication authentication;

        try {
            authentication = authenticationService.authenticate(
                    loginRequest.identifier(),
                    loginRequest.password()
            );
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or phone number or password."
            );
        }

        CustomerPrincipal principal =
                (CustomerPrincipal) authentication.getPrincipal();

        CustomerAccount account = findAccount(principal.getCustomerId());

        sessionAuthenticationStrategy.onAuthentication(
                authentication,
                request,
                response
        );

        request.getSession(true).setMaxInactiveInterval(30 * 60);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                request,
                response
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(CustomerAccountResponse.from(account));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerAccountResponse> currentCustomer(
            @AuthenticationPrincipal CustomerPrincipal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in."
            );
        }

        CustomerAccount account = findAccount(principal.getCustomerId());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(CustomerAccountResponse.from(account));
    }

    private CustomerAccount findAccount(Long customerId) {
        return accountRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Please sign in again."
                ));
    }
}