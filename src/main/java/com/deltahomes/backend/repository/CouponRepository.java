package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.commerce.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    @Query(value = """
            SELECT * FROM coupons c
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ c.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM coupons c
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ c.search_vector)
            """,
            nativeQuery = true)
    Page<Coupon> searchIndex(@Param("q") String q, Pageable pageable);
}
