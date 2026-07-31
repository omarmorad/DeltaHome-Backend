package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.auth.OtpCode;
import com.deltahomes.backend.entity.enums.OtpPurpose;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Generates, sends, verifies and consumes SMS/email OTP codes.
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

    @Value("${app.admin.permanent-otp:}")
    private String permanentOtp;

    public OtpService(OtpCodeRepository otpCodeRepository,
                      SmsService smsService,
                      EmailService emailService) {
        this.otpCodeRepository = otpCodeRepository;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    public int getExpiryMinutes() {
        return expiryMinutes;
    }

    // ---------- SMS (phone) OTP ----------

    @Transactional
    public OtpCode sendOtp(String phone, OtpPurpose purpose) {
        if (phone.equals(adminPhone) && !permanentOtp.isBlank()) {
            log.info("[PERMANENT ADMIN OTP] SMS OTP for {}: {}", phone, permanentOtp);
            return null;
        }
        return issueOtp(phone, null, purpose, code -> smsService.sendOtp(phone, code));
    }

    @Transactional
    public void verify(String phone, String code, OtpPurpose purpose) {
        if (isPermanentOtp(phone, code)) {
            return;
        }
        checkOtp(phone, null, code, purpose);
    }

    /** Deletes all phone codes for the recipient + purpose so they cannot be replayed. */
    @Transactional
    public void consume(String phone, OtpPurpose purpose) {
        otpCodeRepository.deleteByPhoneAndPurpose(phone, purpose);
    }

    // ---------- Email OTP ----------

    @Transactional
    public OtpCode sendEmailOtp(String email, OtpPurpose purpose) {
        return issueOtp(null, email, purpose, code -> emailService.sendOtp(email, code));
    }

    @Transactional
    public void verifyEmail(String email, String code, OtpPurpose purpose) {
        checkOtp(null, email, code, purpose);
    }

    /** Deletes all email codes for the recipient + purpose so they cannot be replayed. */
    @Transactional
    public void consumeEmail(String email, OtpPurpose purpose) {
        otpCodeRepository.deleteByEmailAndPurpose(email, purpose);
    }

    // ---------- Shared logic ----------

    private OtpCode issueOtp(String phone, String email, OtpPurpose purpose, Consumer<String> sender) {
        LocalDateTime now = LocalDateTime.now();
        String recipient = phone != null ? phone : email;

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

        // Invalidate any previously issued code for this recipient + purpose.
        if (phone != null) {
            otpCodeRepository.deleteByPhoneAndPurpose(phone, purpose);
        } else {
            otpCodeRepository.deleteByEmailAndPurpose(email, purpose);
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

        sender.accept(code);
        return otpCodeRepository.save(otp);
    }

    private void checkOtp(String phone, String email, String code, OtpPurpose purpose) {
        Optional<OtpCode> found = phone != null
                ? otpCodeRepository.findFirstByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose)
                : otpCodeRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);
        OtpCode otp = found.orElseThrow(() -> new BusinessException("No active OTP code found. Request a new one."));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpCodeRepository.delete(otp);
            throw new BusinessException("OTP code has expired. Request a new one.");
        }
        boolean alreadyVerified = Boolean.TRUE.equals(otp.getVerified());

        if (!constantTimeEquals(otp.getCodeHash(), hash(code))) {
            if (alreadyVerified) {
                // A previously verified code is only accepted while the submitted
                // code still matches — guards against replaying a different value.
                throw new BusinessException("Invalid OTP code.");
            }
            if (otp.getAttempts() >= maxAttempts) {
                otpCodeRepository.delete(otp);
                throw new BusinessException("Too many invalid attempts. Request a new code.");
            }
            otp.setAttempts(otp.getAttempts() + 1);
            otpCodeRepository.save(otp);
            throw new BusinessException("Invalid OTP code.");
        }
        if (!alreadyVerified) {
            otp.setVerified(true);
            otpCodeRepository.save(otp);
        }
    }

    /** The permanent admin OTP never expires and works for any purpose. */
    private boolean isPermanentOtp(String phone, String code) {
        return !adminPhone.isBlank()
                && !permanentOtp.isBlank()
                && phone.equals(adminPhone)
                && constantTimeEquals(permanentOtp, code);
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
