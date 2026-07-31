package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReportSummary {

    UUID getId();

    String getEntityType();

    UUID getEntityId();

    String getReason();

    String getStatus();

    String getDecision();

    String getReporterName();

    LocalDateTime getCreatedAt();
}
