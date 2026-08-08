package com.deltahomes.backend.dto.summary;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Safe, flat projection for subscription plan list/search responses.
 */
public record SubscriptionPlanSummary(
        UUID id,
        String name,
        String nameAr,
        String nameEn,
        String tier,
        BigDecimal price,
        Integer listingQuota,
        Integer broadcastQuota,
        Boolean isActive,
        OffsetDateTime createdAt
) {}