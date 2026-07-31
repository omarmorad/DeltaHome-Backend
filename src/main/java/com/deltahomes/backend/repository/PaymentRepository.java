package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.PaymentSummary;
import com.deltahomes.backend.entity.commerce.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query(value = """
            SELECT p.id, p.amount, p.method, p.status, p.gateway_reference AS gatewayReference,
                   p.subscription_id AS subscriptionId, p.created_at AS createdAt
            FROM payments p
            WHERE (CAST(:status AS text) IS NULL OR p.status = CAST(:status AS text))
            """,
            countQuery = """
            SELECT count(*) FROM payments p
            WHERE (CAST(:status AS text) IS NULL OR p.status = CAST(:status AS text))
            """,
            nativeQuery = true)
    Page<PaymentSummary> searchIndex(@Param("status") String status, Pageable pageable);
}
