package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.ReviewSummary;
import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
    boolean existsByReviewerIdAndEntityTypeAndEntityId(UUID reviewerId, EntityType entityType, UUID entityId);

    long countByEntityTypeAndEntityId(EntityType entityType, UUID entityId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r " +
            "WHERE r.entityType = :entityType AND r.entityId = :entityId")
    Double averageRating(@Param("entityType") EntityType entityType, @Param("entityId") UUID entityId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
            "WHERE r.entityType = :entityType AND r.entityId = :entityId GROUP BY r.rating")
    List<Object[]> ratingDistribution(@Param("entityType") EntityType entityType,
                                      @Param("entityId") UUID entityId);

    /**
     * Index query with eager fetching of reviewer relationship.
     * Uses JPQL with @EntityGraph to avoid LazyInitializationException.
     */
    @EntityGraph(attributePaths = {"reviewer"})
    @Query("SELECT r FROM Review r " +
           "WHERE (:entityType IS NULL OR r.entityType = :entityType) " +
           "AND (:entityId IS NULL OR r.entityId = :entityId) " +
           "AND (:minRating IS NULL OR r.rating >= :minRating) " +
           "AND (:q = '' OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Review> searchIndex(@Param("q") String q,
                             @Param("entityType") EntityType entityType,
                             @Param("entityId") UUID entityId,
                             @Param("minRating") Integer minRating,
                             Pageable pageable);
}
