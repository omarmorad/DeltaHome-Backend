package com.deltahomes.backend.dto.summary;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Projection of a coupon for admin listings — never exposes the raw entity. */
public record CouponSummary(
        UUID id,
        String code,
        Byte discountPercent,
        LocalDate validFrom,
        LocalDate validTo,
        Integer maxUses,
        OffsetDateTime createdAt
) {
}
