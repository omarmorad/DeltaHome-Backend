package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentSummary {

    UUID getId();

    String getStatus();

    LocalDateTime getRequestedSlot();

    String getNote();

    LocalDateTime getCreatedAt();

    UUID getPropertyId();

    String getPropertyTitle();

    String getCustomerName();

    String getOwnerName();
}
