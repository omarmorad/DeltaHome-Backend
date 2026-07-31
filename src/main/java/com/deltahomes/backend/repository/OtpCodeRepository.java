package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.auth.OtpCode;
import com.deltahomes.backend.entity.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findFirstByPhoneAndPurposeOrderByCreatedAtDesc(String phone, OtpPurpose purpose);

    void deleteByPhoneAndPurpose(String phone, OtpPurpose purpose);

    long countByPhoneAndPurposeAndCreatedAtAfter(String phone, OtpPurpose purpose, LocalDateTime after);
}
