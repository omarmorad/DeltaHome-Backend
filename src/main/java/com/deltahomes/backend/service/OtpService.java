package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.auth.OtpCode;
import com.deltahomes.backend.entity.enums.OtpPurpose;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.OtpCodeRepository;
import com.deltahomes.backend.repository.UserRepository;
import com.deltahomes.backend.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Generates, sends, verifies and consumes SMS/email OTP codes.
 * <p>
 * Delivery preference: SMTP (email) OTP is the primary channel for new users;
 * SMS OTP is used for phones that already belong to a registered user.
 * <p>
 * Security measures:
 * <ul>
 *   <li>Codes are stored as SHA-256 hashes, never in plain text.</li>
 *   <li>Codes expire after a short window (default 5 minutes).</li>
 *   <li>Brute force is limited by a max attempts counter per code.</li>
 *   <li>Send frequency is rate-limited per recipient + purpose.</li>
 *   <li>A successful verification or a full flow consumes (deletes) the code.</li>
 * </ul>
 */
@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpCodeRepository otpCodeRepository;
    private final SmsService smsService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.max-sends-per-window:5}")
    private int maxSendsPerWindow;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.admin.phone:}")
    private String adminPhone;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.permanent-otp:}")
    private String permanentOtp;

    public OtpService(OtpCodeRepository otpCodeRepository,
                      SmsService smsService,
                      EmailService emailService,
                      UserRepository userRepository) {
        this.otpCodeRepository = otpCodeRepository;
        this.smsService = smsService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public int getExpiryMinutes() {
        return expiryMinutes;
    }

    // ---------- Unified OTP with SMTP preference ---------
    //
    // Delivery preference:
    //   1. Email identifiers  -> SMTP email OTP (primary channel for new users).
    //   2. Phone identifiers  -> SMS OTP, but ONLY when the user already exists.
    //   3. Unknown phone      -> rejected with guidance to use the email (SMTP) flow,
    //      because we cannot send an SMTP code without an email address.

    @Transactional
    public OtpCode sendOtp(String identifier, OtpPurpose purpose) {
        if (identifier == null || identifier.isBlank()) {
            throw new BusinessException("Email or phone is required");
        }

        // Handle permanent admin OTP
        if ((identifier.equals(adminPhone) || identifier.equals(adminEmail)) && !permanentOtp.isBlank()) {
            log.info("[PERMANENT ADMIN OTP] OTP for {}: {}", identifier, permanentOtp);
            return null;
        }

        if (isEmailFormat(identifier)) {
            // Prefer SMTP/email for OTP delivery
            return issueOtp(null, identifier, purpose, code -> emailService.sendOtp(identifier, code));
        }

        // Phone identifier: SMS OTP is only available once we already have the user.
        Optional<User> existingUser = userRepository.findByPhone(identifier);
        if (existingUser.isPresent()) {
            return issueOtp(identifier, null, purpose, code -> smsService.sendOtp(identifier, code));
        }

        // New (not yet registered) users verify via SMTP email instead.
        throw new BusinessException(purpose == OtpPurpose.REGISTRATION
                ? "Phone OTP is only available for registered users. Please register using your email (SMTP verification)."
                : "No account found for this phone.");
    }

    @Transactional
    public void verify(String identifier, String code, OtpPurpose purpose) {
        if (isPermanentOtp(identifier, code)) {
            return;
        }

        if (isEmailFormat(identifier)) {
            checkOtp(null, identifier, code, purpose);
        } else {
            checkOtp(identifier, null, code, purpose);
        }
    }

    /** Deletes all codes for the recipient + purpose so they cannot be replayed. */
    @Transactional
    public void consume(String identifier, OtpPurpose purpose) {
        if (isEmailFormat(identifier)) {
            otpCodeRepository.deleteByEmailAndPurpose(identifier, purpose);
        } else {
            otpCodeRepository.deleteByPhoneAndPurpose(identifier, purpose);
        }
    }

    // Helper methods
    private boolean isEmailFormat(String input) {
        return input != null && input.contains("@") && input.indexOf('@') > 0;
    }

    // ---------- Shared logic ----------

    private OtpCode issueOtp(String phone, String email, OtpPurpose purpose, Consumer<String> sender) {
        OffsetDateTime now = OffsetDateTime.now();

        // Rate limit counts EVERY code issued in the window. Previous codes are
        // invalidated (not deleted) precisely so this count stays accurate.
        long sentInWindow = phone != null
                ? otpCodeRepository.countByPhoneAndPurposeAndCreatedAtAfter(phone, purpose, now.minusMinutes(15))
                : otpCodeRepository.countByEmailAndPurposeAndCreatedAtAfter(email, purpose, now.minusMinutes(15));
        if (sentInWindow >= maxSendsPerWindow) {
            throw new BusinessException("Too many OTP requests. Please try again later.");
        }

        Optional<OtpCode> last = phone != null
                ? otpCodeRepository.findFirstByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose)
                : otpCodeRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);
        last.ifPresent(previous -> {
            if (previous.getCreatedAt().isAfter(now.minusSeconds(resendCooldownSeconds))) {
                throw new BusinessException("Please wait a moment before requesting a new code.");
            }
        });

        // Invalidate any previously issued codes for this recipient + purpose.
        // They are kept so the send-rate limit can count them; only the newest
        // non-invalidated code is ever accepted by checkOtp.
        if (phone != null) {
            otpCodeRepository.invalidateByPhoneAndPurpose(phone, purpose);
        } else {
            otpCodeRepository.invalidateByEmailAndPurpose(email, purpose);
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        OtpCode otp = new OtpCode();
        otp.setPhone(phone);
        otp.setEmail(email);
        otp.setCodeHash(hash(code));
        otp.setPurpose(purpose);
        otp.setExpiresAt(now.plusMinutes(expiryMinutes));
        otp.setAttempts(0);
        otp.setVerified(false);

        OtpCode saved = otpCodeRepository.save(otp);

        // Send outside the DB transaction: SMTP/SMS I/O must not hold a pooled
        // connection. If the transaction rolls back the code is simply not sent.
        // Outside a transaction (tests, non-tx callers) fall back to sending now.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sender.accept(code);
                }
            });
        } else {
            sender.accept(code);
        }
        return saved;
    }

    private void checkOtp(String phone, String email, String code, OtpPurpose purpose) {
        Optional<OtpCode> found = phone != null
                ? otpCodeRepository.findFirstByPhoneAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(phone, purpose)
                : otpCodeRepository.findFirstByEmailAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(email, purpose);
        OtpCode otp = found.orElseThrow(() -> new BusinessException("No active OTP code found. Request a new one."));

        if (otp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            otp.setInvalidated(true);
            otpCodeRepository.save(otp);
            throw new BusinessException("OTP code has expired. Request a new one.");
        }

        if (!constantTimeEquals(otp.getCodeHash(), hash(code))) {
            if (otp.getAttempts() >= maxAttempts) {
                otp.setInvalidated(true);
                otpCodeRepository.save(otp);
                throw new BusinessException("Too many invalid attempts. Request a new code.");
            }
            otp.setAttempts(otp.getAttempts() + 1);
            otpCodeRepository.save(otp);
            throw new BusinessException("Invalid OTP code.");
        }

        // Consume on successful verification: the code cannot be replayed,
        // even if a later flow step fails or the caller never consumes it.
        otp.setVerified(true);
        otp.setInvalidated(true);
        otpCodeRepository.save(otp);
    }

    /** The permanent admin OTP never expires and works for any purpose. */
    private boolean isPermanentOtp(String identifier, String code) {
        return !permanentOtp.isBlank()
                && constantTimeEquals(permanentOtp, code)
                && (
                        (!adminPhone.isBlank() && identifier.equals(adminPhone))
                        ||
                        (!adminEmail.isBlank() && identifier.equals(adminEmail))
                );
    }

    private static String hash(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
