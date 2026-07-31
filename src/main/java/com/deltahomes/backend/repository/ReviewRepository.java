package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.ReviewSummary;
import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByEntityTypeAndEntityId(EntityType entityType, UUID entityId);
    boolean existsByReviewerIdAndSourceAppointmentId(UUID reviewerId, UUID sourceAppointmentId);

    @Query(value = """
            SELECT r.id, r.entity_type AS entityType, r.entity_id AS entityId, r.rating, r.comment,
                   r.interaction_verified AS interactionVerified, r.created_at AS createdAt,
                   u.name AS reviewerName
            FROM reviews r
            JOIN users u ON u.id = r.reviewer_id
            WHERE (CAST(:entityType AS text) IS NULL OR r.entity_type = CAST(:entityType AS text))
              AND (CAST(:entityId AS uuid) IS NULL OR r.entity_id = CAST(:entityId AS uuid))
              AND (CAST(:minRating AS smallint) IS NULL OR r.rating >= CAST(:minRating AS smallint))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ r.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM reviews r
            WHERE (CAST(:entityType AS text) IS NULL OR r.entity_type = CAST(:entityType AS text))
              AND (CAST(:entityId AS uuid) IS NULL OR r.entity_id = CAST(:entityId AS uuid))
              AND (CAST(:minRating AS smallint) IS NULL OR r.rating >= CAST(:minRating AS smallint))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ r.search_vector)
            """,
            nativeQuery = true)
    Page<ReviewSummary> searchIndex(@Param("q") String q,
                                    @Param("entityType") String entityType,
                                    @Param("entityId") UUID entityId,
                                    @Param("minRating") Integer minRating,
                                    Pageable pageable);
}
