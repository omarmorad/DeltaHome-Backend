package com.deltahomes.backend.dto.appointment;

import com.deltahomes.backend.entity.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Request/response payloads for the appointments module.
 */
public final class AppointmentDtos {

    private AppointmentDtos() {
    }

    public record CreateAppointmentRequest(
            @NotNull(message = "propertyId is required")
            UUID propertyId,

            @NotNull(message = "requestedSlot is required")
            OffsetDateTime requestedSlot,

            @Size(max = 255, message = "Note must be at most 255 characters")
            String note
    ) {
    }

    public record UpdateAppointmentStatusRequest(
            @NotNull(message = "Status is required")
            AppointmentStatus status
    ) {
    }
}
