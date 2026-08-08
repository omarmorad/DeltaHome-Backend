package com.deltahomes.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * JPA configuration.
 * <p>
 * Auditing is enabled via {@code @EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")}
 * on {@code DeltaHomeBackendApplication}. The default provider yields {@link java.time.LocalDateTime},
 * which cannot be converted to the {@link OffsetDateTime} fields used by {@code BaseEntity}
 * ({@code @CreatedDate}/{@code @LastModifiedDate}); this bean supplies {@code OffsetDateTime} directly.
 */
@Configuration
public class JpaConfig {

    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
