package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ConversationSummary {

    UUID getId();

    String getLastMessagePreview();

    LocalDateTime getUpdatedAt();

    UUID getOtherUserId();

    String getOtherUserName();
}
