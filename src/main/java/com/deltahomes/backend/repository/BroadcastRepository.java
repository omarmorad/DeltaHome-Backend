package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.marketing.Broadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BroadcastRepository extends JpaRepository<Broadcast, UUID> {
    List<Broadcast> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    long countByCompanyId(UUID companyId);
}
