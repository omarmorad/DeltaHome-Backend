package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface VerificationSummary {

    UUID getId();

    String getType();

    String getStatus();

    String getDocumentUrl();

    String getRejectionReason();

    OffsetDateTime getReviewedAt();

    OffsetDateTime getCreatedAt();

    String getUserName();

    String getReviewedByName();
}
