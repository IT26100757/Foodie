package com.foodie.setup;

import com.foodie.model.Admin;
import com.foodie.repository.AdminRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("admin-setup")
public class AdminSetupRunner implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSetupRunner(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String email = System.getenv("FOODIE_ADMIN_EMAIL");
        String password = System.getenv("FOODIE_ADMIN_PASSWORD");

        if (email == null || email.isBlank()
                || password == null || password.length() < 12) {
            throw new IllegalStateException(
                    "Admin setup requires an email and a password of at least 12 characters"
            );
        }

        email = email.trim();

        if (adminRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalStateException(
                    "An admin account already exists with this email"
            );
        }

        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setEnabled(true);

        adminRepository.save(admin);

        System.out.println("Platform Admin account created successfully.");
    }
}