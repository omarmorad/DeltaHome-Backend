package com.deltahomes.backend.dto.property;

import com.deltahomes.backend.entity.enums.FinishingLevel;
import com.deltahomes.backend.entity.enums.HideReason;
import com.deltahomes.backend.entity.enums.PropertyPurpose;
import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.enums.Readiness;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Request/response payloads for the property module. Entities never cross the
 * controller boundary — this DTO set replaces the previous raw-{@code Property}
 * request/response handling (which allowed mass assignment of server-controlled
 * fields such as {@code status} and {@code isFeatured}).
 */
public final class PropertyDtos {

    private PropertyDtos() {
    }

    // ---------- Requests ----------

    public record CreatePropertyRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 200, message = "Title must be at most 200 characters")
            String title,

            /** Arabic title — optional; used when serving Arabic-locale requests. */
            @Size(max = 200, message = "Arabic title must be at most 200 characters")
            String titleAr,

            @Size(max = 10000, message = "Description must be at most 10000 characters")
            String description,

            /** Arabic description — optional; used when serving Arabic-locale requests. */
            @Size(max = 10000, message = "Arabic description must be at most 10000 characters")
            String descriptionAr,

            @NotNull(message = "Price is required")
            @Positive(message = "Price must be positive")
            BigDecimal price,

            @NotNull(message = "Purpose is required")
            PropertyPurpose purpose,

            @NotBlank(message = "Category is required")
            @Size(max = 30, message = "Category must be at most 30 characters")
            String category,

            @NotNull(message = "City is required")
            UUID cityId,

            @NotNull(message = "District is required")
            UUID districtId,

            @Size(max = 200, message = "Street must be at most 200 characters")
            String street,

            BigDecimal latitude,

            BigDecimal longitude,

            @NotNull(message = "Readiness is required")
            Readiness readiness,

            FinishingLevel finishingLevel,

            @Size(max = 10000, message = "Features must be at most 10000 characters")
            String features
    ) {
    }

    /** All fields optional — only non-null fields are applied to the entity. */
    public record UpdatePropertyRequest(
            @Size(max = 200, message = "Title must be at most 200 characters")
            String title,

            @Size(max = 200, message = "Arabic title must be at most 200 characters")
            String titleAr,

            @Size(max = 10000, message = "Description must be at most 10000 characters")
            String description,

            @Size(max = 10000, message = "Arabic description must be at most 10000 characters")
            String descriptionAr,

            @Positive(message = "Price must be positive")
            BigDecimal price,

            PropertyPurpose purpose,

            @Size(max = 30, message = "Category must be at most 30 characters")
            String category,

            UUID cityId,

            UUID districtId,

            @Size(max = 200, message = "Street must be at most 200 characters")
            String street,

            BigDecimal latitude,

            BigDecimal longitude,

            Readiness readiness,

            FinishingLevel finishingLevel,

            @Size(max = 10000, message = "Features must be at most 10000 characters")
            String features
    ) {
    }

    // ---------- Responses ----------

    /**
     * Full property projection. `title`/`description` are resolved for the
     * request locale (Arabic when serving `ar`, falling back to the base text);
     * the raw Arabic values are always included for editing. Owner identity is
     * limited to id/name — sensitive owner fields are intentionally omitted.
     */
    public record PropertyDetailResponse(
            UUID id,
            String title,
            String titleAr,
            String description,
            String descriptionAr,
            BigDecimal price,
            PropertyPurpose purpose,
            String category,
            UUID cityId,
            String cityName,
            UUID districtId,
            String districtName,
            String street,
            BigDecimal latitude,
            BigDecimal longitude,
            PropertyStatus status,
            HideReason hideReason,
            Boolean isFeatured,
            Readiness readiness,
            FinishingLevel finishingLevel,
            String features,
            OwnerInfo owner,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {

        public record OwnerInfo(UUID id, String name) {
        }
    }
}
