package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.BroadcastSummary;
import com.deltahomes.backend.entity.marketing.Broadcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BroadcastRepository extends JpaRepository<Broadcast, UUID> {
    List<Broadcast> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    long countByCompanyId(UUID companyId);

    @Query(value = """
            SELECT b.id, b.title, b.body, b.type, b.created_at AS createdAt,
                   c.id AS companyId, c.name AS companyName
            FROM broadcasts b
            JOIN companies c ON c.id = b.company_id
            WHERE (CAST(:companyId AS uuid) IS NULL OR b.company_id = CAST(:companyId AS uuid))
              AND (CAST(:type AS text) IS NULL OR b.type = CAST(:type AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ b.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM broadcasts b
            WHERE (CAST(:companyId AS uuid) IS NULL OR b.company_id = CAST(:companyId AS uuid))
              AND (CAST(:type AS text) IS NULL OR b.type = CAST(:type AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ b.search_vector)
            """,
            nativeQuery = true)
    Page<BroadcastSummary> searchIndex(@Param("q") String q,
                                       @Param("companyId") UUID companyId,
                                       @Param("type") String type,
                                       Pageable pageable);
}
