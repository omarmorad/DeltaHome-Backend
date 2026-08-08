package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ConversationSummary {

    UUID getId();

    String getLastMessagePreview();

    OffsetDateTime getUpdatedAt();

    UUID getOtherUserId();

    String getOtherUserName();
}
