package com.deltahomes.backend.entity.auth;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One-time password (SMS OTP) code for phone verification during
 * registration, login, and password reset. The code itself is never stored
 * in plain text — only its SHA-256 hash is persisted.
 */
@Getter
@Setter
@Entity
@Table(name = "otp_codes", indexes = {
        @Index(name = "idx_otp_phone_purpose", columnList = "phone, purpose"),
        @Index(name = "idx_otp_email_purpose", columnList = "email, purpose")
})
public class OtpCode extends BaseEntity {

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "code_hash", length = 64, nullable = false)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 30, nullable = false)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;
}
