package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface MessageSummary {

    UUID getId();

    String getType();

    String getTextBody();

    String getMediaUrl();

    String getPayload();

    String getSenderName();

    OffsetDateTime getCreatedAt();
}
