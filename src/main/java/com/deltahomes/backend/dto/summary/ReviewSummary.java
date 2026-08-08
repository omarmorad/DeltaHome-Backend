package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ReviewSummary {

    UUID getId();

    String getEntityType();

    UUID getEntityId();

    Byte getRating();

    String getComment();

    Boolean getInteractionVerified();

    String getReviewerName();

    OffsetDateTime getCreatedAt();
}
