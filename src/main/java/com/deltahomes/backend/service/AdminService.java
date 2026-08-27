package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.AuditLogSummary;
import com.deltahomes.backend.dto.summary.BroadcastSummary;
import com.deltahomes.backend.dto.summary.CouponSummary;
import com.deltahomes.backend.dto.summary.FraudFlagSummary;
import com.deltahomes.backend.dto.summary.PaymentSummary;
import com.deltahomes.backend.dto.summary.ReportSummary;
import com.deltahomes.backend.dto.summary.SubscriptionSummary;
import com.deltahomes.backend.dto.summary.UserSummary;
import com.deltahomes.backend.dto.summary.VerificationSummary;
import com.deltahomes.backend.entity.admin.AuditLog;
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
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.entity.user.Verification;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.AuditLogRepository;
import com.deltahomes.backend.repository.BroadcastRepository;
import com.deltahomes.backend.repository.CouponRepository;
import com.deltahomes.backend.repository.FraudFlagRepository;
import com.deltahomes.backend.repository.PaymentRepository;
import com.deltahomes.backend.repository.ReportRepository;
import com.deltahomes.backend.repository.SubscriptionRepository;
import com.deltahomes.backend.repository.UserRepository;
import com.deltahomes.backend.repository.VerificationRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final VerificationRepository verificationRepository;
    private final FraudFlagRepository fraudFlagRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BroadcastRepository broadcastRepository;

    public AdminService(UserRepository userRepository,
                        ReportRepository reportRepository,
                        VerificationRepository verificationRepository,
                        FraudFlagRepository fraudFlagRepository,
                        CouponRepository couponRepository,
                        PaymentRepository paymentRepository,
                        AuditLogRepository auditLogRepository,
                        SubscriptionRepository subscriptionRepository,
                        BroadcastRepository broadcastRepository) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.verificationRepository = verificationRepository;
        this.fraudFlagRepository = fraudFlagRepository;
        this.couponRepository = couponRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogRepository = auditLogRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.broadcastRepository = broadcastRepository;
    }

    public PaginatedResponse<UserSummary> indexUsers(String q, UserRole role, UserStatus status,
                                                     Pageable pageable) {
        Page<UserSummary> page = userRepository.searchIndex(
                q == null ? "" : q.trim(),
                role == null ? null : role.name(),
                status == null ? null : status.name(),
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<ReportSummary> indexReports(String q, ReportStatus status, EntityType entityType,
                                                         Pageable pageable) {
        Page<ReportSummary> page = reportRepository.searchIndex(
                q == null ? "" : q.trim(),
                status == null ? null : status.name(),
                entityType == null ? null : entityType.name(),
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<VerificationSummary> indexVerifications(VerificationStatus status,
                                                                     VerificationType type,
                                                                     Pageable pageable) {
        Page<VerificationSummary> page = verificationRepository.searchIndex(
                status == null ? null : status.name(),
                type == null ? null : type.name(),
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<FraudFlagSummary> indexFraudFlags(FraudFlagType flagType, String status,
                                                               Pageable pageable) {
        Page<FraudFlagSummary> page = fraudFlagRepository.searchIndex(
                        flagType == null ? null : flagType.name(),
                        status,
                        PageUtils.normalizeSort(pageable))
                .map(this::toFraudFlagSummary);
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<CouponSummary> indexCoupons(String q, Pageable pageable) {
        Page<CouponSummary> page = couponRepository.searchIndex(
                        q == null ? "" : q.trim(), PageUtils.normalizeSort(pageable))
                .map(this::toCouponSummary);
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<PaymentSummary> indexPayments(PaymentStatus status, Pageable pageable) {
        Page<PaymentSummary> page = paymentRepository.searchIndex(
                status == null ? null : status.name(), PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<AuditLogSummary> indexAuditLogs(String q, UUID adminId, Pageable pageable) {
        Page<AuditLogSummary> page = auditLogRepository.searchIndex(
                q == null ? "" : q.trim(), adminId, PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<SubscriptionSummary> indexSubscriptions(SubscriptionStatus status,
                                                                     Pageable pageable) {
        Page<SubscriptionSummary> page = subscriptionRepository.searchIndex(
                status == null ? null : status.name(), PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<BroadcastSummary> indexBroadcasts(String q, UUID companyId, BroadcastType type,
                                                               Pageable pageable) {
        Page<BroadcastSummary> page = broadcastRepository.searchIndex(
                        q == null ? "" : q.trim(),
                        companyId,
                        type,
                        PageUtils.normalizeSort(pageable))
                .map(this::toBroadcastSummary);
        return PaginatedResponse.from(page);
    }

    // ---------- Verification decisions ----------

    /**
     * Applies an admin decision (APPROVE/REJECT) to a pending verification and
     * records the action in the audit log. Rejected verifications require a
     * reason.
     */
    @Transactional
    public VerificationSummary decideVerification(User admin, UUID verificationId,
                                                  String decision, String reason) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification", verificationId));
        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new BusinessException("This verification has already been decided");
        }

        VerificationStatus status = switch (decision == null ? "" : decision.trim().toUpperCase()) {
            case "APPROVE", "ACCEPTED" -> VerificationStatus.ACCEPTED;
            case "REJECT", "REJECTED" -> VerificationStatus.REJECTED;
            default -> throw new BusinessException("decision must be APPROVE or REJECT");
        };
        if (status == VerificationStatus.REJECTED && (reason == null || reason.isBlank())) {
            throw new BusinessException("A rejection reason is required");
        }

        verification.setStatus(status);
        verification.setRejectionReason(status == VerificationStatus.REJECTED ? reason.trim() : null);
        verification.setReviewedBy(admin);
        verification.setReviewedAt(OffsetDateTime.now());
        Verification saved = verificationRepository.save(verification);

        AuditLog auditLog = new AuditLog();
        auditLog.setAdmin(admin);
        auditLog.setAction("VERIFICATION_" + status.name());
        auditLog.setTargetType("VERIFICATION");
        auditLog.setTargetId(saved.getId());
        auditLog.setReason(status == VerificationStatus.REJECTED ? saved.getRejectionReason() : null);
        auditLogRepository.save(auditLog);

        return toVerificationSummary(saved);
    }

    // ---------- Mappers ----------

    private FraudFlagSummary toFraudFlagSummary(FraudFlag f) {
        return new FraudFlagSummary(
                f.getId(),
                f.getEntityType() != null ? f.getEntityType().name() : null,
                f.getEntityId(),
                f.getFlagType() != null ? f.getFlagType().name() : null,
                f.getStatus(),
                f.getCreatedAt()
        );
    }

    private CouponSummary toCouponSummary(Coupon c) {
        return new CouponSummary(
                c.getId(),
                c.getCode(),
                c.getDiscountPercent(),
                c.getValidFrom(),
                c.getValidTo(),
                c.getMaxUses(),
                c.getCreatedAt()
        );
    }

    private BroadcastSummary toBroadcastSummary(com.deltahomes.backend.entity.marketing.Broadcast b) {
        return new BroadcastSummary() {
            @Override public UUID getId() { return b.getId(); }
            @Override public String getTitle() { return b.getTitle(); }
            @Override public String getBody() { return b.getBody(); }
            @Override public String getType() { return b.getType() != null ? b.getType().name() : null; }
            @Override public OffsetDateTime getCreatedAt() { return b.getCreatedAt(); }
            @Override public UUID getCompanyId() {
                return b.getCompany() != null ? b.getCompany().getId() : null;
            }
            @Override public String getCompanyName() {
                return b.getCompany() != null ? b.getCompany().getName() : null;
            }
        };
    }

    private VerificationSummary toVerificationSummary(Verification v) {
        return new VerificationSummary() {
            @Override public UUID getId() { return v.getId(); }
            @Override public String getType() { return v.getType() != null ? v.getType().name() : null; }
            @Override public String getStatus() { return v.getStatus() != null ? v.getStatus().name() : null; }
            @Override public String getDocumentUrl() { return v.getDocumentUrl(); }
            @Override public String getRejectionReason() { return v.getRejectionReason(); }
            @Override public OffsetDateTime getReviewedAt() { return v.getReviewedAt(); }
            @Override public OffsetDateTime getCreatedAt() { return v.getCreatedAt(); }
            @Override public String getUserName() {
                return v.getUser() != null ? v.getUser().getName() : null;
            }
            @Override public String getReviewedByName() {
                return v.getReviewedBy() != null ? v.getReviewedBy().getName() : null;
            }
        };
    }
}
