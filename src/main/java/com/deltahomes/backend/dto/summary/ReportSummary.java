package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ReportSummary {

    UUID getId();

    String getEntityType();

    UUID getEntityId();

    String getReason();

    String getStatus();

    String getDecision();

    String getReporterName();

    OffsetDateTime getCreatedAt();
}
