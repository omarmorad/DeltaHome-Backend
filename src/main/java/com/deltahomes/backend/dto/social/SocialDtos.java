package com.deltahomes.backend.dto.social;

import com.deltahomes.backend.entity.SavedItem;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.CompanyType;
import com.deltahomes.backend.entity.enums.EntityType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request/response payloads for the Saved Items & Follow module.
 * Responses are safe projections — they never expose users' password hashes.
 */
public final class SocialDtos {

    private SocialDtos() {
    }

    public record SavedItemResponse(
            Long id,
            EntityType entityType,
            Long entityId,
            LocalDateTime createdAt
    ) {
        public static SavedItemResponse from(SavedItem item) {
            return new SavedItemResponse(item.getId(), item.getEntityType(), item.getEntityId(), item.getCreatedAt());
        }
    }

    public record CompanySummaryResponse(
            Long id,
            String name,
            CompanyType type,
            String logoUrl,
            String coverUrl,
            String phone,
            String whatsapp,
            String email,
            String website,
            Boolean verified,
            Integer followersCount,
            BigDecimal reputationScore
    ) {
        public static CompanySummaryResponse from(Company company) {
            return new CompanySummaryResponse(
                    company.getId(),
                    company.getName(),
                    company.getType(),
                    company.getLogoUrl(),
                    company.getCoverUrl(),
                    company.getPhone(),
                    company.getWhatsapp(),
                    company.getEmail(),
                    company.getWebsite(),
                    company.getVerified(),
                    company.getFollowersCount(),
                    company.getReputationScore()
            );
        }
    }
}
