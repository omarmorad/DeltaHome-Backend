package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.SubscriptionSummary;
import com.deltahomes.backend.entity.commerce.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query(value = """
            SELECT s.id, s.status, s.start_date AS startDate, s.end_date AS endDate,
                   s.user_id AS userId, s.company_id AS companyId, s.created_at AS createdAt,
                   sp.name AS planName
            FROM subscriptions s
            JOIN subscription_plans sp ON sp.id = s.plan_id
            WHERE (CAST(:status AS text) IS NULL OR s.status = CAST(:status AS text))
            """,
            countQuery = """
            SELECT count(*) FROM subscriptions s
            WHERE (CAST(:status AS text) IS NULL OR s.status = CAST(:status AS text))
            """,
            nativeQuery = true)
    Page<SubscriptionSummary> searchIndex(@Param("status") String status, Pageable pageable);
}
