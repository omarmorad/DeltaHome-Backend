package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.ReportSummary;
import com.deltahomes.backend.entity.moderation.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    @Query(value = """
            SELECT r.id, r.entity_type AS entityType, r.entity_id AS entityId,
                   r.reason, r.status, r.decision, r.created_at AS createdAt,
                   u.name AS reporterName
            FROM reports r
            JOIN users u ON u.id = r.reporter_id
            WHERE (CAST(:status AS text) IS NULL OR r.status = CAST(:status AS text))
              AND (CAST(:entityType AS text) IS NULL OR r.entity_type = CAST(:entityType AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ r.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM reports r
            WHERE (CAST(:status AS text) IS NULL OR r.status = CAST(:status AS text))
              AND (CAST(:entityType AS text) IS NULL OR r.entity_type = CAST(:entityType AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ r.search_vector)
            """,
            nativeQuery = true)
    Page<ReportSummary> searchIndex(@Param("q") String q,
                                    @Param("status") String status,
                                    @Param("entityType") String entityType,
                                    Pageable pageable);
}
