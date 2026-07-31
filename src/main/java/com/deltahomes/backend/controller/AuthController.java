package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.auth.AuthDtos;
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
    public ResponseEntity<AuthDtos.OtpSendResponse> sendOtp(
            @Valid @RequestBody AuthDtos.SendOtpRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request.phone(), request.purpose()));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthDtos.OtpVerifyResponse> verifyOtp(
            @Valid @RequestBody AuthDtos.VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request.phone(), request.code(), request.purpose()));
    }

    @PostMapping("/otp/send-email")
    public ResponseEntity<AuthDtos.OtpSendResponse> sendEmailOtp(
            @Valid @RequestBody AuthDtos.SendEmailOtpRequest request) {
        return ResponseEntity.ok(authService.sendEmailOtp(request.email(), request.purpose()));
    }

    @PostMapping("/otp/verify-email")
    public ResponseEntity<AuthDtos.OtpVerifyResponse> verifyEmailOtp(
            @Valid @RequestBody AuthDtos.VerifyEmailOtpRequest request) {
        return ResponseEntity.ok(authService.verifyEmailOtp(request.email(), request.code(), request.purpose()));
    }

    // ---------- Registration & login ----------

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/register-email")
    public ResponseEntity<AuthDtos.AuthResponse> registerWithEmail(
            @Valid @RequestBody AuthDtos.RegisterEmailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerWithEmail(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(
            @Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/login/email")
    public ResponseEntity<AuthDtos.AuthResponse> loginWithEmail(
            @Valid @RequestBody AuthDtos.LoginEmailRequest request) {
        return ResponseEntity.ok(authService.loginWithEmail(request));
    }

    @PostMapping("/login/otp")
    public ResponseEntity<AuthDtos.AuthResponse> loginWithOtp(
            @Valid @RequestBody AuthDtos.LoginWithOtpRequest request) {
        return ResponseEntity.ok(authService.loginWithOtp(request.phone(), request.otpCode()));
    }

    @PostMapping("/login/otp/email")
    public ResponseEntity<AuthDtos.AuthResponse> loginWithEmailOtp(
            @Valid @RequestBody AuthDtos.LoginWithEmailOtpRequest request) {
        return ResponseEntity.ok(authService.loginWithEmailOtp(request.email(), request.otpCode()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDtos.AuthResponse> refresh(
            @Valid @RequestBody AuthDtos.RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    // ---------- Profile ----------

    @GetMapping("/me")
    public ResponseEntity<AuthDtos.UserResponse> me(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(authService.me(principal.getUsername()));
    }

    // ---------- Password ----------

    @PutMapping("/password")
    public ResponseEntity<AuthDtos.MessageResponse> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        authService.changePassword(principal.getUsername(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(new AuthDtos.MessageResponse("Password changed successfully"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<AuthDtos.MessageResponse> resetPassword(
            @Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request.phone(), request.otpCode(), request.newPassword());
        return ResponseEntity.ok(new AuthDtos.MessageResponse("Password reset successfully"));
    }

    @PostMapping("/password/reset/email")
    public ResponseEntity<AuthDtos.MessageResponse> resetPasswordByEmail(
            @Valid @RequestBody AuthDtos.ResetPasswordEmailRequest request) {
        authService.resetPasswordByEmail(request.email(), request.otpCode(), request.newPassword());
        return ResponseEntity.ok(new AuthDtos.MessageResponse("Password reset successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthDtos.MessageResponse> logout() {
        // Stateless JWT: the client simply discards the tokens.
        // Server-side invalidation can be added later via a Redis token blacklist.
        return ResponseEntity.ok(new AuthDtos.MessageResponse("Logged out"));
    }
}
