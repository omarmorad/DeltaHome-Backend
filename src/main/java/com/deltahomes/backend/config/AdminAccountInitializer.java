package com.deltahomes.backend.config;

import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.entity.enums.UserStatus;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provisions the permanent bootstrap admin account (phone + role ADMIN) on
 * startup if it does not already exist. The account is never re-created or
 * overwritten afterwards, so any edits made in the DB are preserved.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.phone:}")
    private String adminPhone;

    @Value("${app.admin.name:Delta Admin}")
    private String adminName;

    @Value("${app.admin.email:admin@deltahomes.app}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    public AdminAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminPhone.isBlank()) {
            return;
        }
        if (userRepository.existsByPhone(adminPhone)) {
            // Self-heal: older versions created the admin without an email.
            // Backfill it now so email-based admin login/OTP works.
            userRepository.findByPhone(adminPhone).ifPresent(existing -> {
                if ((existing.getEmail() == null || existing.getEmail().isBlank()) && !adminEmail.isBlank()) {
                    existing.setEmail(adminEmail);
                    userRepository.save(existing);
                    log.info("Backfilled admin email to {}", adminEmail);
                }
            });
            return;
        }

        User user = new User();
        user.setName(adminName);
        user.setPhone(adminPhone);
        user.setEmail(adminEmail);
        user.setPasswordHash(passwordEncoder.encode(adminPassword));
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationLevel((byte) 0);
        userRepository.save(user);

        log.info("Created permanent admin account for phone {}", adminPhone);
    }
}
