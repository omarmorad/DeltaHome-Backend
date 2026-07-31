package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.AuditLogSummary;
import com.deltahomes.backend.dto.summary.BroadcastSummary;
import com.deltahomes.backend.dto.summary.PaymentSummary;
import com.deltahomes.backend.dto.summary.ReportSummary;
import com.deltahomes.backend.dto.summary.SubscriptionSummary;
import com.deltahomes.backend.dto.summary.UserSummary;
import com.deltahomes.backend.dto.summary.VerificationSummary;
import com.deltahomes.backend.entity.commerce.Coupon;
import com.deltahomes.backend.entity.enums.BroadcastType;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.enums.FraudFlagType;
import com.deltahomes.backend.entity.enums.PaymentStatus;
import com.deltahomes.backend.entity.enums.ReportStatus;
import com.deltahomes.backend.entity.enums.SubscriptionStatus;
import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.entity.enums.UserStatus;
import com.deltahomes.backend.entity.enums.VerificationStatus;
import com.deltahomes.backend.entity.enums.VerificationType;
import com.deltahomes.backend.entity.moderation.FraudFlag;
import com.deltahomes.backend.service.AdminService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<PaginatedResponse<UserSummary>> indexUsers(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexUsers(q, role, status, pageable));
    }

    @GetMapping("/reports")
    public ResponseEntity<PaginatedResponse<ReportSummary>> indexReports(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) EntityType entityType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexReports(q, status, entityType, pageable));
    }

    @GetMapping("/verifications")
    public ResponseEntity<PaginatedResponse<VerificationSummary>> indexVerifications(
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) VerificationType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexVerifications(status, type, pageable));
    }

    @GetMapping("/fraud-flags")
    public ResponseEntity<PaginatedResponse<FraudFlag>> indexFraudFlags(
            @RequestParam(required = false) FraudFlagType flagType,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexFraudFlags(flagType, status, pageable));
    }

    @GetMapping("/coupons")
    public ResponseEntity<PaginatedResponse<Coupon>> indexCoupons(
            @RequestParam(defaultValue = "") String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexCoupons(q, pageable));
    }

    @GetMapping("/payments")
    public ResponseEntity<PaginatedResponse<PaymentSummary>> indexPayments(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexPayments(status, pageable));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<PaginatedResponse<AuditLogSummary>> indexAuditLogs(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UUID adminId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexAuditLogs(q, adminId, pageable));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<PaginatedResponse<SubscriptionSummary>> indexSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexSubscriptions(status, pageable));
    }

    @GetMapping("/broadcasts")
    public ResponseEntity<PaginatedResponse<BroadcastSummary>> indexBroadcasts(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) BroadcastType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.indexBroadcasts(q, companyId, type, pageable));
    }

    @PostMapping("/verification/{id}/decision")
    public ResponseEntity<Map<String, String>> decideVerification(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(Map.of(
            "verificationId", String.valueOf(id),
            "decision", request.get("decision")
        ));
    }
}
