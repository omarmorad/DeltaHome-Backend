package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.marketing.Broadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {
    List<Broadcast> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    long countByCompanyId(Long companyId);
}
