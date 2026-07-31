package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.user.Verification;
import com.deltahomes.backend.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {
    List<Verification> findByUserId(Long userId);
    List<Verification> findByStatus(VerificationStatus status);
}
