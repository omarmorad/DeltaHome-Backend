package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationSummary {

    UUID getId();

    String getTitle();

    String getBody();

    String getType();

    String getEntityType();

    UUID getEntityId();

    Boolean getIsRead();

    LocalDateTime getCreatedAt();
}
