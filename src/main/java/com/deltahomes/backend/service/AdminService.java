package com.deltahomes.backend.service;

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

    public PaginatedResponse<FraudFlag> indexFraudFlags(FraudFlagType flagType, String status,
                                                        Pageable pageable) {
        Page<FraudFlag> page = fraudFlagRepository.searchIndex(
                flagType == null ? null : flagType.name(),
                status,
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<Coupon> indexCoupons(String q, Pageable pageable) {
        Page<Coupon> page = couponRepository.searchIndex(
                q == null ? "" : q.trim(), PageUtils.normalizeSort(pageable));
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
                type == null ? null : type.name(),
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }
}
