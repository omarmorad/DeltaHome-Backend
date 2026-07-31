package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReviewSummary {

    UUID getId();

    String getEntityType();

    UUID getEntityId();

    Byte getRating();

    String getComment();

    Boolean getInteractionVerified();

    String getReviewerName();

    LocalDateTime getCreatedAt();
}
