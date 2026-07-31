package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.CompanySummary;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Page<Company> findByType(CompanyType type, Pageable pageable);
    Page<Company> findByVerifiedTrue(Pageable pageable);

    @Query(value = """
            SELECT c.id, c.name, c.type, c.logo_url AS logoUrl, c.cover_url AS coverUrl,
                   c.phone, c.whatsapp, c.email, c.website, c.verified,
                   c.followers_count AS followersCount, c.reputation_score AS reputationScore
            FROM companies c
            WHERE (CAST(:verified AS boolean) IS NULL OR c.verified = CAST(:verified AS boolean))
              AND (CAST(:type AS text) IS NULL OR c.type = CAST(:type AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ c.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM companies c
            WHERE (CAST(:verified AS boolean) IS NULL OR c.verified = CAST(:verified AS boolean))
              AND (CAST(:type AS text) IS NULL OR c.type = CAST(:type AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ c.search_vector)
            """,
            nativeQuery = true)
    Page<CompanySummary> searchIndex(@Param("q") String q,
                                     @Param("type") String type,
                                     @Param("verified") Boolean verified,
                                     Pageable pageable);
}
