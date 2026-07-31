package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.VerificationSummary;
import com.deltahomes.backend.entity.user.Verification;
import com.deltahomes.backend.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {
    List<Verification> findByUserId(UUID userId);
    List<Verification> findByStatus(VerificationStatus status);

    @Query(value = """
            SELECT v.id, v.type, v.status, v.document_url AS documentUrl,
                   v.rejection_reason AS rejectionReason, v.reviewed_at AS reviewedAt,
                   v.created_at AS createdAt, u.name AS userName, rb.name AS reviewedByName
            FROM verifications v
            JOIN users u ON u.id = v.user_id
            LEFT JOIN users rb ON rb.id = v.reviewed_by
            WHERE (CAST(:status AS text) IS NULL OR v.status = CAST(:status AS text))
              AND (CAST(:type AS text) IS NULL OR v.type = CAST(:type AS text))
            """,
            countQuery = """
            SELECT count(*) FROM verifications v
            WHERE (CAST(:status AS text) IS NULL OR v.status = CAST(:status AS text))
              AND (CAST(:type AS text) IS NULL OR v.type = CAST(:type AS text))
            """,
            nativeQuery = true)
    Page<VerificationSummary> searchIndex(@Param("status") String status,
                                          @Param("type") String type,
                                          Pageable pageable);
}
