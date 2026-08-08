package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Safe, flat projection for service list/search responses.
 */
public record ServiceSummary(
        UUID id,
        String name,
        String nameAr,
        String nameEn,
        String category,
        String iconUrl,
        OffsetDateTime createdAt
) {}