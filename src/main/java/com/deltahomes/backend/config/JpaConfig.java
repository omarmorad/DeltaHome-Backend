package com.deltahomes.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration enabling auditing for BaseEntity.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}