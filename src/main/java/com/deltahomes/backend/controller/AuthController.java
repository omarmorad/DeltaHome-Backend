package com.deltahomes.backend.controller;

import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> request) {
        User user = authService.register(
                request.get("name"),
                request.get("phone"),
                request.get("password"),
                UserRole.valueOf(request.getOrDefault("role", "CUSTOMER").toUpperCase())
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> request) {
        // Stub: SMS OTP verification via Twilio
        return ResponseEntity.ok(Map.of("status", "verified"));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> request) {
        User user = authService.login(request.get("phone"), request.get("password"));
        return ResponseEntity.ok(user);
    }
}
