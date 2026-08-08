package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.auth.AuthDtos;
import com.deltahomes.backend.entity.enums.OtpPurpose;
import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.entity.enums.UserStatus;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.UserRepository;
import com.deltahomes.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       OtpService otpService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.authenticationManager = authenticationManager;
    }

    // ---------- OTP ----------

    @Transactional
    public AuthDtos.OtpSendResponse sendOtp(String phone, OtpPurpose purpose) {
        otpService.sendOtp(phone, purpose);
        return new AuthDtos.OtpSendResponse(phone, otpService.getExpiryMinutes(),
                "OTP sent to " + maskPhone(phone));
    }

    @Transactional
    public AuthDtos.OtpVerifyResponse verifyOtp(String phone, String code, OtpPurpose purpose) {
        otpService.verify(phone, code, purpose);
        return new AuthDtos.OtpVerifyResponse(phone, true);
    }

    // ---------- Email OTP ----------

    @Transactional
    public AuthDtos.OtpSendResponse sendEmailOtp(String email, OtpPurpose purpose) {
        otpService.sendEmailOtp(normalizeEmail(email), purpose);
        return new AuthDtos.OtpSendResponse(normalizeEmail(email), otpService.getExpiryMinutes(),
                "OTP sent to " + maskEmail(email));
    }

    @Transactional
    public AuthDtos.OtpVerifyResponse verifyEmailOtp(String email, String code, OtpPurpose purpose) {
        otpService.verifyEmail(normalizeEmail(email), code, purpose);
        return new AuthDtos.OtpVerifyResponse(normalizeEmail(email), true);
    }

    // ---------- Email registration ----------

    @Transactional
    public AuthDtos.AuthResponse registerWithEmail(AuthDtos.RegisterEmailRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email already registered");
        }
        if (request.role() == UserRole.ADMIN) {
            throw new BusinessException("Admin accounts are provisioned by the platform");
        }

        otpService.verifyEmail(email, request.otpCode(), OtpPurpose.REGISTRATION);

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationLevel((byte) 1);
        user.setLastLoginAt(OffsetDateTime.now());

        User saved = userRepository.save(user);
        otpService.consumeEmail(email, OtpPurpose.REGISTRATION);
        return buildAuthResponse(saved);
    }

    // ---------- Email login ----------

    @Transactional
    public AuthDtos.AuthResponse loginWithEmail(AuthDtos.LoginEmailRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        ensureActive(user);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizeEmail(request.email()), request.password()));
        } catch (AuthenticationException e) {
            throw new BusinessException("Invalid credentials");
        }

        touchLastLogin(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse loginWithEmailOtp(String email, String otpCode) {
        String normalized = normalizeEmail(email);
        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new BusinessException("No account found for this email. Please register first."));
        ensureActive(user);

        otpService.verifyEmail(normalized, otpCode, OtpPurpose.LOGIN);

        touchLastLogin(user);
        otpService.consumeEmail(normalized, OtpPurpose.LOGIN);
        return buildAuthResponse(user);
    }

    // ---------- Email password reset ----------

    @Transactional
    public void resetPasswordByEmail(String email, String otpCode, String newPassword) {
        String normalized = normalizeEmail(email);
        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new BusinessException("No account found for this email"));
        otpService.verifyEmail(normalized, otpCode, OtpPurpose.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpService.consumeEmail(normalized, OtpPurpose.PASSWORD_RESET);
    }

    // ---------- Registration ----------

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException("Phone number already registered");
        }
        if (request.role() == UserRole.ADMIN) {
            throw new BusinessException("Admin accounts are provisioned by the platform");
        }

        otpService.verify(request.phone(), request.otpCode(), OtpPurpose.REGISTRATION);

        User user = new User();
        user.setName(request.name().trim());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationLevel((byte) 0);
        user.setLastLoginAt(OffsetDateTime.now());

        User saved = userRepository.save(user);
        otpService.consume(request.phone(), OtpPurpose.REGISTRATION);
        return buildAuthResponse(saved);
    }

    // ---------- Login ----------

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        ensureActive(user);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.phone(), request.password()));
        } catch (AuthenticationException e) {
            throw new BusinessException("Invalid credentials");
        }

        touchLastLogin(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse loginWithOtp(String phone, String otpCode) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("No account found for this phone. Please register first."));
        ensureActive(user);

        otpService.verify(phone, otpCode, OtpPurpose.LOGIN);

        touchLastLogin(user);
        otpService.consume(phone, OtpPurpose.LOGIN);
        return buildAuthResponse(user);
    }

    // ---------- Tokens ----------

    public AuthDtos.AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }
        String phone = jwtService.extractPhone(refreshToken);
        User user = findByPhoneOrEmail(phone)
                .orElseThrow(() -> new BusinessException("Invalid or expired refresh token"));
        ensureActive(user);
        return buildAuthResponse(user);
    }

    // ---------- Profile & password ----------

    public AuthDtos.UserResponse me(String identifier) {
        User user = findByPhoneOrEmail(identifier)
                .orElseThrow(() -> new BusinessException("User not found"));
        return AuthDtos.UserResponse.from(user);
    }

    @Transactional
    public void changePassword(String phone, String currentPassword, String newPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String phone, String otpCode, String newPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("No account found for this phone"));
        otpService.verify(phone, otpCode, OtpPurpose.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpService.consume(phone, OtpPurpose.PASSWORD_RESET);
    }

    // ---------- Helpers ----------

    private void ensureActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is " + user.getStatus().name().toLowerCase());
        }
    }

    private void touchLastLogin(User user) {
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    private AuthDtos.AuthResponse buildAuthResponse(User user) {
        return new AuthDtos.AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                AuthDtos.UserResponse.from(user)
        );
    }

    private Optional<User> findByPhoneOrEmail(String identifier) {
        return userRepository.findByPhone(identifier)
                .or(() -> userRepository.findByEmail(identifier));
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.substring(0, 2) + "***" + local.substring(local.length() - 1) + domain;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
