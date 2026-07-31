package com.deltahomes.backend.dto.summary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Safe, flat projection for property list/search responses.
 * Never exposes the owner entity (and thus the owner's password hash).
 */
public interface PropertySummary {

    UUID getId();

    String getTitle();

    String getDescription();

    BigDecimal getPrice();

    String getPurpose();

    String getCategory();

    String getStatus();

    String getCityName();

    String getDistrictName();

    String getStreet();

    String getReadiness();

    String getFinishingLevel();

    Boolean getIsFeatured();

    String getFeatures();

    LocalDateTime getCreatedAt();
}
