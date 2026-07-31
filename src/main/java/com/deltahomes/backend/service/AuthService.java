package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.entity.enums.UserStatus;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String name, String phone, String password, UserRole role) {
        if (userRepository.existsByPhone(phone)) {
            throw new BusinessException("Phone number already registered");
        }

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationLevel((byte) 0);

        return userRepository.save(user);
    }

    public User login(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is " + user.getStatus().name().toLowerCase());
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }
}
