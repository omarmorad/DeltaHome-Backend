package com.deltahomes.backend.dto.summary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentSummary {

    UUID getId();

    BigDecimal getAmount();

    String getMethod();

    String getStatus();

    String getGatewayReference();

    UUID getSubscriptionId();

    LocalDateTime getCreatedAt();
}
