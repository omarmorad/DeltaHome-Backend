package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, UUID> {
    Optional<Follower> findByUserIdAndCompanyId(UUID userId, UUID companyId);
    boolean existsByUserIdAndCompanyId(UUID userId, UUID companyId);
    long countByCompanyId(UUID companyId);
    void deleteByUserIdAndCompanyId(UUID userId, UUID companyId);
    List<Follower> findByUserId(UUID userId);
}
