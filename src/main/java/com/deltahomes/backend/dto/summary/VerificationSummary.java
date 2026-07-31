package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface VerificationSummary {

    UUID getId();

    String getType();

    String getStatus();

    String getDocumentUrl();

    String getRejectionReason();

    LocalDateTime getReviewedAt();

    LocalDateTime getCreatedAt();

    String getUserName();

    String getReviewedByName();
}
