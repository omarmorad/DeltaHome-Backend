package com.deltahomes.backend.dto.social;

import com.deltahomes.backend.entity.SavedItem;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.CompanyType;
import com.deltahomes.backend.entity.enums.EntityType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Request/response payloads for the Saved Items & Follow module.
 * Responses are safe projections — they never expose users' password hashes.
 */
public final class SocialDtos {

    private SocialDtos() {
    }

    public record SavedItemResponse(
            UUID id,
            EntityType entityType,
            UUID entityId,
            OffsetDateTime createdAt
    ) {
        public static SavedItemResponse from(SavedItem item) {
            return new SavedItemResponse(item.getId(), item.getEntityType(), item.getEntityId(), item.getCreatedAt());
        }
    }

    public record CompanySummaryResponse(
            UUID id,
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

    /** Response body for {@code POST /api/v1/companies/{id}/follow}. */
    public record FollowResponse(
            UUID companyId,
            String companyName,
            boolean following
    ) {
    }
}
