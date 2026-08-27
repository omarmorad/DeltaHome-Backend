package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.ApiResponse;
import com.deltahomes.backend.dto.summary.AuditLogSummary;
import com.deltahomes.backend.dto.summary.BroadcastSummary;
import com.deltahomes.backend.dto.summary.CouponSummary;
import com.deltahomes.backend.dto.summary.FraudFlagSummary;
import com.deltahomes.backend.dto.summary.PaymentSummary;
import com.deltahomes.backend.dto.summary.ReportSummary;
import com.deltahomes.backend.dto.summary.SubscriptionSummary;
import com.deltahomes.backend.dto.summary.UserSummary;
import com.deltahomes.backend.dto.summary.VerificationSummary;
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
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.service.AdminService;
import com.deltahomes.backend.service.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserContext userContext;

    public AdminController(AdminService adminService, UserContext userContext) {
        this.adminService = adminService;
        this.userContext = userContext;
    }

    /** Body for the verification decision endpoint. */
    public record VerificationDecisionRequest(
            @NotBlank(message = "Decision is required (APPROVE or REJECT)")
            String decision,

            String reason
    ) {
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<java.util.List<UserSummary>>> indexUsers(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexUsers(q, role, status, pageable)));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<java.util.List<ReportSummary>>> indexReports(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) EntityType entityType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexReports(q, status, entityType, pageable)));
    }

    @GetMapping("/verifications")
    public ResponseEntity<ApiResponse<java.util.List<VerificationSummary>>> indexVerifications(
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) VerificationType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexVerifications(status, type, pageable)));
    }

    @GetMapping("/fraud-flags")
    public ResponseEntity<ApiResponse<java.util.List<FraudFlagSummary>>> indexFraudFlags(
            @RequestParam(required = false) FraudFlagType flagType,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexFraudFlags(flagType, status, pageable)));
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<java.util.List<CouponSummary>>> indexCoupons(
            @RequestParam(defaultValue = "") String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexCoupons(q, pageable)));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<java.util.List<PaymentSummary>>> indexPayments(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexPayments(status, pageable)));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<java.util.List<AuditLogSummary>>> indexAuditLogs(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UUID adminId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexAuditLogs(q, adminId, pageable)));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<java.util.List<SubscriptionSummary>>> indexSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexSubscriptions(status, pageable)));
    }

    @GetMapping("/broadcasts")
    public ResponseEntity<ApiResponse<java.util.List<BroadcastSummary>>> indexBroadcasts(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) BroadcastType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(adminService.indexBroadcasts(q, companyId, type, pageable)));
    }

    @PostMapping("/verification/{id}/decision")
    public ResponseEntity<ApiResponse<VerificationSummary>> decideVerification(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody VerificationDecisionRequest request) {
        User admin = userContext.currentUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(adminService.decideVerification(
                admin, id, request.decision(), request.reason())));
    }
}
