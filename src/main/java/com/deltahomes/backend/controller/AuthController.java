package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.auth.AuthDtos;
import com.deltahomes.backend.dto.common.ApiResponse;
import com.deltahomes.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ---------- OTP ----------

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<AuthDtos.OtpSendResponse>> sendOtp(
            @Valid @RequestBody AuthDtos.SendOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.sendOtp(request.phone(), request.purpose())));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthDtos.OtpVerifyResponse>> verifyOtp(
            @Valid @RequestBody AuthDtos.VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.verifyOtp(request.phone(), request.code(), request.purpose())));
    }

    @PostMapping("/otp/send-email")
    public ResponseEntity<ApiResponse<AuthDtos.OtpSendResponse>> sendEmailOtp(
            @Valid @RequestBody AuthDtos.SendEmailOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.sendEmailOtp(request.email(), request.purpose())));
    }

    @PostMapping("/otp/verify-email")
    public ResponseEntity<ApiResponse<AuthDtos.OtpVerifyResponse>> verifyEmailOtp(
            @Valid @RequestBody AuthDtos.VerifyEmailOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.verifyEmailOtp(request.email(), request.code(), request.purpose())));
    }

    // ---------- Registration & login ----------

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/register-email")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> registerWithEmail(
            @Valid @RequestBody AuthDtos.RegisterEmailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.registerWithEmail(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> login(
            @Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/login/email")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> loginWithEmail(
            @Valid @RequestBody AuthDtos.LoginEmailRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginWithEmail(request)));
    }

    @PostMapping("/login/otp")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> loginWithOtp(
            @Valid @RequestBody AuthDtos.LoginWithOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginWithOtp(request.phone(), request.otpCode())));
    }

    @PostMapping("/login/otp/email")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> loginWithEmailOtp(
            @Valid @RequestBody AuthDtos.LoginWithEmailOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginWithEmailOtp(request.email(), request.otpCode())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> refresh(
            @Valid @RequestBody AuthDtos.RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request.refreshToken())));
    }

    // ---------- Profile ----------

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDtos.UserResponse>> me(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(authService.me(principal.getUsername())));
    }

    // ---------- Password ----------

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        authService.changePassword(principal.getUsername(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message("Password changed successfully"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request.phone(), request.otpCode(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message("Password reset successfully"));
    }

    @PostMapping("/password/reset/email")
    public ResponseEntity<ApiResponse<Void>> resetPasswordByEmail(
            @Valid @RequestBody AuthDtos.ResetPasswordEmailRequest request) {
        authService.resetPasswordByEmail(request.email(), request.otpCode(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message("Password reset successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails principal) {
        // Revokes the stored refresh-token jti server-side; access tokens
        // remain valid until they expire (stateless).
        authService.logout(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.message("Logged out"));
    }
}
