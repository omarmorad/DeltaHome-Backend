package com.deltahomes.backend.dto.summary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserSummary {

    UUID getId();

    String getName();

    String getPhone();

    String getEmail();

    String getRole();

    String getStatus();

    Byte getVerificationLevel();

    LocalDateTime getCreatedAt();

    LocalDateTime getLastLoginAt();
}
