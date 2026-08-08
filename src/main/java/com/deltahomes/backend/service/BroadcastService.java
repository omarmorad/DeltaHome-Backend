package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.BroadcastSummary;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.BroadcastType;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import com.deltahomes.backend.entity.marketing.Broadcast;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.BroadcastRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BroadcastService {

    private final BroadcastRepository broadcastRepository;

    public BroadcastService(BroadcastRepository broadcastRepository) {
        this.broadcastRepository = broadcastRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<BroadcastSummary> index(String q, UUID companyId, BroadcastType type,
                                                     Pageable pageable) {
        Page<BroadcastSummary> page = broadcastRepository.searchIndex(
                q == null ? "" : q.trim(),
                companyId,
                type,
                PageUtils.normalizeSort(pageable))
            .map(this::toSummary);
        return PaginatedResponse.from(page);
    }

    @Transactional
    public Broadcast createBroadcast(Company company, Broadcast broadcast) {
        // Quota enforcement
        long currentMonthCount = broadcastRepository.countByCompanyId(company.getId());
        int maxAllowed = getBroadcastCap(company.getPlan());

        if (currentMonthCount >= maxAllowed) {
            throw new BusinessException(
                "Broadcast quota exceeded. Plan allows " + maxAllowed + " per month.");
        }

        broadcast.setCompany(company);
        return broadcastRepository.save(broadcast);
    }

    private BroadcastSummary toSummary(Broadcast b) {
        return new BroadcastSummary() {
            @Override public UUID getId() { return b.getId(); }
            @Override public String getTitle() { return b.getTitle(); }
            @Override public String getBody() { return b.getBody(); }
            @Override public String getType() { return b.getType() != null ? b.getType().name() : null; }
            @Override public java.time.OffsetDateTime getCreatedAt() { return b.getCreatedAt(); }
            @Override public UUID getCompanyId() { return b.getCompany() != null ? b.getCompany().getId() : null; }
            @Override public String getCompanyName() { return b.getCompany() != null ? b.getCompany().getName() : null; }
        };
    }

    private int getBroadcastCap(SubscriptionTier plan) {
        return switch (plan) {
            case BASIC -> 0;
            case PREMIUM -> 10;
            case ENTERPRISE -> 50;
        };
    }
}
