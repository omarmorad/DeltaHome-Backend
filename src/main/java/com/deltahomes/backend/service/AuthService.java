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
        String normalized = normalizeEmail(email);
        otpService.sendOtp(normalized, purpose);
        return new AuthDtos.OtpSendResponse(normalized, otpService.getExpiryMinutes(),
                "OTP sent to " + maskEmail(normalized));
    }

    @Transactional
    public AuthDtos.OtpVerifyResponse verifyEmailOtp(String email, String code, OtpPurpose purpose) {
        String normalized = normalizeEmail(email);
        otpService.verify(normalized, code, purpose);
        return new AuthDtos.OtpVerifyResponse(normalized, true);
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

        otpService.verify(email, request.otpCode(), OtpPurpose.REGISTRATION);

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationLevel((byte) 1);
        user.setLastLoginAt(OffsetDateTime.now());

        User saved = userRepository.save(user);
        otpService.consume(email, OtpPurpose.REGISTRATION);
        return buildAuthResponse(saved);
    }

    // ---------- Email login ----------

    @Transactional
    public AuthDtos.AuthResponse loginWithEmail(AuthDtos.LoginEmailRequest request) {
        String email = normalizeEmail(request.email());
        Optional<User> found = userRepository.findByEmail(email);
        if (found.isEmpty()) {
            // Burn a BCrypt round so that "unknown email" and "wrong password"
            // take the same time (prevents account enumeration via timing).
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            throw new BusinessException("Invalid credentials");
        }
        User user = found.get();
        ensureActive(user);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
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

        otpService.verify(normalized, otpCode, OtpPurpose.LOGIN);

        touchLastLogin(user);
        otpService.consume(normalized, OtpPurpose.LOGIN);
        return buildAuthResponse(user);
    }

    // ---------- Email password reset ----------

    @Transactional
    public void resetPasswordByEmail(String email, String otpCode, String newPassword) {
        String normalized = normalizeEmail(email);
        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new BusinessException("No account found for this email"));
        otpService.verify(normalized, otpCode, OtpPurpose.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        revokeRefreshToken(user);
        userRepository.save(user);
        otpService.consume(normalized, OtpPurpose.PASSWORD_RESET);
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

    /**
     * Refresh-token rotation: the presented token must be a valid, unexpired
     * refresh token whose jti matches the one stored for the user. A mismatch
     * means the token was already rotated (or revoked) — treated as theft and
     * rejected. On success a brand-new access + refresh pair is issued and the
     * stored jti is replaced.
     */
    @Transactional
    public AuthDtos.AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }
        String jti = jwtService.extractJti(refreshToken);
        String identifier = jwtService.extractPhone(refreshToken);
        User user = findByPhoneOrEmail(identifier)
                .orElseThrow(() -> new BusinessException("Invalid or expired refresh token"));
        ensureActive(user);

        if (jti == null || !jti.equals(user.getRefreshTokenId())) {
            // Rotated/revoked token reuse — reject. The stored jti is kept so
            // the legitimate session keeps working; repeated reuse of the old
            // token cannot succeed.
            throw new BusinessException("Invalid or expired refresh token");
        }

        AuthDtos.AuthResponse response = buildAuthResponse(user);
        userRepository.save(user); // persist rotated jti set by buildAuthResponse
        return response;
    }

    /**
     * Server-side logout: clears the stored refresh-token jti so the current
     * refresh token can no longer be exchanged. Access tokens remain valid
     * until they expire (stateless).
     */
    @Transactional
    public void logout(String identifier) {
        findByPhoneOrEmail(identifier).ifPresent(user -> {
            revokeRefreshToken(user);
            userRepository.save(user);
        });
    }

    // ---------- Profile & password ----------

    public AuthDtos.UserResponse me(String identifier) {
        User user = findByPhoneOrEmail(identifier)
                .orElseThrow(() -> new BusinessException("User not found"));
        return AuthDtos.UserResponse.from(user);
    }

    @Transactional
    public void changePassword(String identifier, String currentPassword, String newPassword) {
        validateNewPassword(newPassword);
        User user = findByPhoneOrEmail(identifier)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        revokeRefreshToken(user);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String phone, String otpCode, String newPassword) {
        validateNewPassword(newPassword);
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("No account found for this phone"));
        otpService.verify(phone, otpCode, OtpPurpose.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        revokeRefreshToken(user);
        userRepository.save(user);
        otpService.consume(phone, OtpPurpose.PASSWORD_RESET);
    }

    // ---------- Helpers ----------

    /** Pre-computed BCrypt hash of an unguessable random value — used only to equalize login timing. */
    private static final String DUMMY_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMye.IjPeGqBQKzE1v0mR91fWtCwQOcFkOa";

    private void ensureActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is " + user.getStatus().name().toLowerCase());
        }
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 72) {
            throw new BusinessException(AuthDtos.PASSWORD_MESSAGE);
        }
    }

    private void revokeRefreshToken(User user) {
        user.setRefreshTokenId(null);
    }

    private void touchLastLogin(User user) {
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    /** Issues the token pair and stores the new refresh jti on the user (caller must save within its transaction). */
    private AuthDtos.AuthResponse buildAuthResponse(User user) {
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setRefreshTokenId(jwtService.extractJti(refreshToken));
        return new AuthDtos.AuthResponse(
                jwtService.generateAccessToken(user),
                refreshToken,
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
