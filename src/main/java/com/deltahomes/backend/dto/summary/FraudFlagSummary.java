package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Projection of a fraud flag for admin listings — never exposes the raw entity. */
public record FraudFlagSummary(
        UUID id,
        String entityType,
        UUID entityId,
        String flagType,
        String status,
        OffsetDateTime createdAt
) {
}
