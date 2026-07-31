package com.deltahomes.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports() {
        return ResponseEntity.ok(Map.of("reports", "Admin reports pending"));
    }

    @PostMapping("/verification/{id}/decision")
    public ResponseEntity<Map<String, String>> decideVerification(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(Map.of(
            "verificationId", String.valueOf(id),
            "decision", request.get("decision")
        ));
    }
}
