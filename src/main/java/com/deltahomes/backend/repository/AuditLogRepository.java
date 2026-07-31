package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.AuditLogSummary;
import com.deltahomes.backend.entity.admin.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query(value = """
            SELECT al.id, al.action, al.target_type AS targetType, al.target_id AS targetId,
                   al.ip_address AS ipAddress, al.reason, al.created_at AS createdAt,
                   a.name AS adminName
            FROM audit_logs al
            JOIN users a ON a.id = al.admin_id
            WHERE (CAST(:adminId AS uuid) IS NULL OR al.admin_id = CAST(:adminId AS uuid))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ al.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM audit_logs al
            WHERE (CAST(:adminId AS uuid) IS NULL OR al.admin_id = CAST(:adminId AS uuid))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ al.search_vector)
            """,
            nativeQuery = true)
    Page<AuditLogSummary> searchIndex(@Param("q") String q,
                                      @Param("adminId") UUID adminId,
                                      Pageable pageable);
}
