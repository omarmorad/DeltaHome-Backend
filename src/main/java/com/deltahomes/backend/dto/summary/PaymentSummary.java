package com.deltahomes.backend.dto.summary;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface PaymentSummary {

    UUID getId();

    BigDecimal getAmount();

    String getMethod();

    String getStatus();

    String getGatewayReference();

    UUID getSubscriptionId();

    OffsetDateTime getCreatedAt();
}
