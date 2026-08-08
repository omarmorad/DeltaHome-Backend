package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Safe, flat projection for feature list/search responses.
 */
public record FeatureSummary(
        UUID id,
        String name,
        String nameAr,
        String nameEn,
        String icon,
        OffsetDateTime createdAt
) {}