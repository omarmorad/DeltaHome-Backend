package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Safe, flat projection for district list/search responses.
 */
public record DistrictSummary(
        UUID id,
        String name,
        String nameAr,
        UUID cityId,
        String cityName,
        String cityNameAr,
        OffsetDateTime createdAt
) {}