package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Safe, flat projection for city list/search responses.
 */
public record CitySummary(
        UUID id,
        String name,
        String nameAr,
        Boolean isActive,
        OffsetDateTime createdAt
) {}