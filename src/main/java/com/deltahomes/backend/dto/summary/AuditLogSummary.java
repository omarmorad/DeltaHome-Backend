package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogSummary {

    UUID getId();

    String getAction();

    String getTargetType();

    UUID getTargetId();

    String getIpAddress();

    String getReason();

    String getAdminName();

    LocalDateTime getCreatedAt();
}
