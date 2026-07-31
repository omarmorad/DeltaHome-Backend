package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.commerce.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    @Query(value = """
            SELECT * FROM subscription_plans sp
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ sp.search_vector)
              AND (CAST(:tier AS text) IS NULL OR sp.tier = CAST(:tier AS text))
              AND (CAST(:isActive AS boolean) IS NULL OR sp.is_active = CAST(:isActive AS boolean))
            """,
            countQuery = """
            SELECT count(*) FROM subscription_plans sp
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ sp.search_vector)
              AND (CAST(:tier AS text) IS NULL OR sp.tier = CAST(:tier AS text))
              AND (CAST(:isActive AS boolean) IS NULL OR sp.is_active = CAST(:isActive AS boolean))
            """,
            nativeQuery = true)
    Page<SubscriptionPlan> searchIndex(@Param("q") String q,
                                       @Param("tier") String tier,
                                       @Param("isActive") Boolean isActive,
                                       Pageable pageable);
}
