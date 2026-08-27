package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.auth.OtpCode;
import com.deltahomes.backend.entity.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findFirstByPhoneAndPurposeOrderByCreatedAtDesc(String phone, OtpPurpose purpose);

    Optional<OtpCode> findFirstByPhoneAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(String phone, OtpPurpose purpose);

    void deleteByPhoneAndPurpose(String phone, OtpPurpose purpose);

    @Modifying
    @Query("UPDATE OtpCode o SET o.invalidated = true WHERE o.phone = :phone AND o.purpose = :purpose")
    void invalidateByPhoneAndPurpose(@Param("phone") String phone, @Param("purpose") OtpPurpose purpose);

    long countByPhoneAndPurposeAndCreatedAtAfter(String phone, OtpPurpose purpose, OffsetDateTime after);

    Optional<OtpCode> findFirstByEmailAndPurposeOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    Optional<OtpCode> findFirstByEmailAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    void deleteByEmailAndPurpose(String email, OtpPurpose purpose);

    @Modifying
    @Query("UPDATE OtpCode o SET o.invalidated = true WHERE o.email = :email AND o.purpose = :purpose")
    void invalidateByEmailAndPurpose(@Param("email") String email, @Param("purpose") OtpPurpose purpose);

    long countByEmailAndPurposeAndCreatedAtAfter(String email, OtpPurpose purpose, OffsetDateTime after);
}
