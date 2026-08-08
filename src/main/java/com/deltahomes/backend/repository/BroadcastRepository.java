package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.BroadcastSummary;
import com.deltahomes.backend.entity.enums.BroadcastType;
import com.deltahomes.backend.entity.marketing.Broadcast;
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
public interface BroadcastRepository extends JpaRepository<Broadcast, UUID> {
    List<Broadcast> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    long countByCompanyId(UUID companyId);

    /**
     * Index query with eager fetching of company relationship.
     * Uses JPQL with @EntityGraph to avoid LazyInitializationException.
     */
    @EntityGraph(attributePaths = {"company"})
    @Query("SELECT b FROM Broadcast b " +
           "WHERE (:companyId IS NULL OR b.company.id = :companyId) " +
           "AND (:type IS NULL OR b.type = :type) " +
           "AND (:q = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.body) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Broadcast> searchIndex(@Param("q") String q,
                                @Param("companyId") UUID companyId,
                                @Param("type") BroadcastType type,
                                Pageable pageable);
}
