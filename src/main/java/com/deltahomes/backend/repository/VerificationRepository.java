package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.user.Verification;
import com.deltahomes.backend.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {
    List<Verification> findByUserId(UUID userId);
    List<Verification> findByStatus(VerificationStatus status);
}
