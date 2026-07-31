package com.deltahomes.backend.dto.auth;

import com.deltahomes.backend.entity.enums.OtpPurpose;
import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.entity.enums.UserStatus;
import com.deltahomes.backend.entity.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * All request/response payloads for the authentication module.
 * Records are grouped here to keep the module cohesive.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public static final String PHONE_PATTERN = "^01[0-9]{9}$";
    public static final String PHONE_MESSAGE = "Phone must be a valid Egyptian number (e.g. 01012345678)";
    public static final String OTP_PATTERN = "^[0-9]{6}$";
    public static final String OTP_MESSAGE = "Code must be 6 digits";
    public static final String PASSWORD_MESSAGE = "Password must be between 6 and 72 characters";

    // ---------- Requests ----------

    public record SendOtpRequest(
            @NotBlank(message = "Phone is required")
            @Pattern(regexp = PHONE_PATTERN, message = PHONE_MESSAGE)
            String phone,

            @NotNull(message = "Purpose is required")
            OtpPurpose purpose
    ) {
    }

    public record VerifyOtpRequest(
            @NotBlank(message = "Phone is required")
            @Pattern(regexp = PHONE_PATTERN, message = PHONE_MESSAGE)
            String phone,

            @NotBlank(message = "Code is required")
            @Pattern(regexp = OTP_PATTERN, message = OTP_MESSAGE)
            String code,

            @NotNull(message = "Purpose is required")
            OtpPurpose purpose
    ) {
    }

    public record RegisterRequest(
            @NotBlank(message = "Name is required")
            @Size(max = 120, message = "Name must be at most 120 characters")
            String name,

            @NotBlank(message = "Phone is required")
            @Pattern(regexp = PHONE_PATTERN, message = PHONE_MESSAGE)
            String phone,

            @NotBlank(message = "Password is required")
            @Size(min = 6, max = 72, message = PASSWORD_MESSAGE)
            String password,

            @NotNull(message = "Role is required")
            UserRole role,

            @NotBlank(message = "OTP code is required")
            @Pattern(regexp = OTP_PATTERN, message = OTP_MESSAGE)
            String otpCode
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Phone is required")
            @Pattern(regexp = PHONE_PATTERN, message = PHONE_MESSAGE)
            String phone,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record LoginWithOtpRequest(
            @NotBlank(message = "Phone is required")
            @Pattern(regexp = PHONE_PATTERN, message = PHONE_MESSAGE)
            String phone,

            @NotBlank(message = "OTP code is required")
            @Pattern(regexp = OTP_PATTERN, message = OTP_MESSAGE)
            String otpCode
    ) {
    }

    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "Current password is required")
            String currentPassword,

            @NotBlank(message = "New password is required")
            @Size(min = 6, max = 72, message = PASSWORD_MESSAGE)
            String newPassword
    ) {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "Phone is required")
            @Pattern(regexp = PHONE_PATTERN, message = PHONE_MESSAGE)
            String phone,

            @NotBlank(message = "OTP code is required")
            @Pattern(regexp = OTP_PATTERN, message = OTP_MESSAGE)
            String otpCode,

            @NotBlank(message = "New password is required")
            @Size(min = 6, max = 72, message = PASSWORD_MESSAGE)
            String newPassword
    ) {
    }

    // ---------- Responses ----------

    public record OtpSendResponse(String phone, int expiresInMinutes, String message) {
    }

    public record OtpVerifyResponse(String phone, boolean verified) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInSeconds,
            UserResponse user
    ) {
    }

    public record UserResponse(
            Long id,
            String name,
            String phone,
            String email,
            String photoUrl,
            UserRole role,
            UserStatus status,
            byte verificationLevel,
            LocalDateTime createdAt
    ) {

        /** Safe projection of a {@link User} — never exposes the password hash. */
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getPhone(),
                    user.getEmail(),
                    user.getPhotoUrl(),
                    user.getRole(),
                    user.getStatus(),
                    user.getVerificationLevel() == null ? 0 : user.getVerificationLevel().byteValue(),
                    user.getCreatedAt()
            );
        }
    }

    public record MessageResponse(String message) {
    }
}
