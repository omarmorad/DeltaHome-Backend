package com.deltahomes.backend.dto.summary;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Safe, flat projection for company list/search responses.
 * Never exposes the owner entity (and thus the owner's password hash).
 */
public interface CompanySummary {

    UUID getId();

    String getName();

    String getType();

    String getLogoUrl();

    String getCoverUrl();

    String getPhone();

    String getWhatsapp();

    String getEmail();

    String getWebsite();

    Boolean getVerified();

    Integer getFollowersCount();

    BigDecimal getReputationScore();
}
