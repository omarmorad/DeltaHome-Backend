package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {
    Optional<Follower> findByUserIdAndCompanyId(Long userId, Long companyId);
    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);
    long countByCompanyId(Long companyId);
    void deleteByUserIdAndCompanyId(Long userId, Long companyId);
    List<Follower> findByUserId(Long userId);
}
