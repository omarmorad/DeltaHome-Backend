package com.deltahomes.backend.dto.summary;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AppointmentSummary {

    UUID getId();

    String getStatus();

    OffsetDateTime getRequestedSlot();

    String getNote();

    OffsetDateTime getCreatedAt();

    UUID getPropertyId();

    String getPropertyTitle();

    String getCustomerName();

    String getOwnerName();
}
