package com.deltahomes.backend.dto.summary;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface SubscriptionSummary {

    UUID getId();

    String getStatus();

    LocalDate getStartDate();

    LocalDate getEndDate();

    OffsetDateTime getCreatedAt();

    UUID getUserId();

    UUID getCompanyId();

    String getPlanName();
}
