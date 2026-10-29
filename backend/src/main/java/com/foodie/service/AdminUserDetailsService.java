package com.foodie.service;

import com.foodie.model.Admin;
import com.foodie.repository.AdminRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid credentials")
                );

        return User.withUsername(admin.getEmail())
                .password(admin.getPasswordHash())
                .roles("ADMIN")
                .disabled(!admin.isEnabled())
                .build();
    }
}