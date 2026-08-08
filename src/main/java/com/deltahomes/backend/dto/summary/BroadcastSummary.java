package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface BroadcastSummary {

    UUID getId();

    String getTitle();

    String getBody();

    String getType();

    OffsetDateTime getCreatedAt();

    UUID getCompanyId();

    String getCompanyName();
}
