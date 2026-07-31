package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MessageSummary {

    UUID getId();

    String getType();

    String getTextBody();

    String getMediaUrl();

    String getPayload();

    String getSenderName();

    LocalDateTime getCreatedAt();
}
