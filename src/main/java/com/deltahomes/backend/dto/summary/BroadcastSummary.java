package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BroadcastSummary {

    UUID getId();

    String getTitle();

    String getBody();

    String getType();

    LocalDateTime getCreatedAt();

    UUID getCompanyId();

    String getCompanyName();
}
