package com.foodie.customer.account;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.List;

public class CustomerPrincipal extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long customerId;

    public CustomerPrincipal(CustomerAccount account) {
        super(
                account.getId().toString(),
                account.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        this.customerId = account.getId();
    }

    public Long getCustomerId() {
        return customerId;
    }
}