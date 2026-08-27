package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.auth.OtpCode;
import com.deltahomes.backend.entity.enums.OtpPurpose;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.OtpCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private EmailService emailService;

    @Mock
    private com.deltahomes.backend.repository.UserRepository userRepository;

    private OtpService otpService;

    @BeforeEach
    void setUp() throws Exception {
        otpService = new OtpService(otpCodeRepository, smsService, emailService, userRepository);
        setField("expiryMinutes", 5);
        setField("maxAttempts", 5);
        setField("maxSendsPerWindow", 5);
        setField("resendCooldownSeconds", 60L);
        setField("adminPhone", "");
        setField("adminEmail", "");
        setField("permanentOtp", "");
    }

    private void setField(String name, Object value) throws Exception {
        var field = OtpService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(otpService, value);
    }

    // ---------- Rate limiting ----------

    @Test
    void sendOtpRejectsWhenSendLimitReached() {
        // 5 sends already recorded in the window (invalidated ones included)
        when(otpCodeRepository.countByEmailAndPurposeAndCreatedAtAfter(
                any(), any(), any())).thenReturn(5L);

        assertThrows(BusinessException.class,
                () -> otpService.sendOtp("user@example.com", OtpPurpose.LOGIN));

        verify(emailService, never()).sendOtp(any(), any());
    }

    @Test
    void sendOtpEnforcesResendCooldown() {
        when(otpCodeRepository.countByEmailAndPurposeAndCreatedAtAfter(
                any(), any(), any())).thenReturn(0L);
        OtpCode recent = new OtpCode();
        recent.setCreatedAt(OffsetDateTime.now().minusSeconds(10));
        when(otpCodeRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(
                "user@example.com", OtpPurpose.LOGIN)).thenReturn(Optional.of(recent));

        assertThrows(BusinessException.class,
                () -> otpService.sendOtp("user@example.com", OtpPurpose.LOGIN));
    }

    @Test
    void sendOtpInvalidatesPreviousCodesInsteadOfDeleting() {
        when(otpCodeRepository.countByEmailAndPurposeAndCreatedAtAfter(
                any(), any(), any())).thenReturn(0L);
        when(otpCodeRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(
                any(), any())).thenReturn(Optional.empty());
        when(otpCodeRepository.save(any(OtpCode.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        otpService.sendOtp("user@example.com", OtpPurpose.LOGIN);

        // Old codes must be invalidated (kept for send counting), not deleted
        verify(otpCodeRepository).invalidateByEmailAndPurpose("user@example.com", OtpPurpose.LOGIN);
        verify(otpCodeRepository, never())
                .deleteByEmailAndPurpose(any(), any());
    }

    @Test
    void sendSmsDeliversCodeAfterIssue() {
        // No active transaction: the sender runs immediately via the
        // non-transactional fallback in issueOtp.
        when(userRepository.findByPhone("01012345678"))
                .thenReturn(Optional.of(new com.deltahomes.backend.entity.user.User()));
        when(otpCodeRepository.countByPhoneAndPurposeAndCreatedAtAfter(
                any(), any(), any())).thenReturn(0L);
        when(otpCodeRepository.findFirstByPhoneAndPurposeOrderByCreatedAtDesc(
                any(), any())).thenReturn(Optional.empty());
        OtpCode saved = new OtpCode();
        when(otpCodeRepository.save(any(OtpCode.class))).thenReturn(saved);

        otpService.sendOtp("01012345678", OtpPurpose.LOGIN);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsService).sendOtp(org.mockito.ArgumentMatchers.eq("01012345678"),
                codeCaptor.capture());
        assertTrue(codeCaptor.getValue().matches("\\d{6}"));
    }

    // ---------- Verification ----------

    private OtpCode activeCode(String plainCode) {
        OtpCode otp = new OtpCode();
        otp.setEmail("user@example.com");
        otp.setPurpose(OtpPurpose.LOGIN);
        otp.setCodeHash(sha256(plainCode));
        otp.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        otp.setAttempts(0);
        otp.setVerified(false);
        otp.setInvalidated(false);
        return otp;
    }

    @Test
    void wrongCodeIncrementsAttemptsEvenForPreviouslyVerifiedCodes() {
        String code = "123456";
        OtpCode otp = activeCode(code);
        otp.setAttempts(3);
        when(otpCodeRepository.findFirstByEmailAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(
                "user@example.com", OtpPurpose.LOGIN)).thenReturn(Optional.of(otp));

        assertThrows(BusinessException.class,
                () -> otpService.verify("user@example.com", "999999", OtpPurpose.LOGIN));
        assertEquals(4, otp.getAttempts());
    }

    @Test
    void successfulVerificationConsumesTheCodeImmediately() {
        String code = "123456";
        OtpCode otp = activeCode(code);
        when(otpCodeRepository.findFirstByEmailAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(
                "user@example.com", OtpPurpose.LOGIN)).thenReturn(Optional.of(otp));

        otpService.verify("user@example.com", code, OtpPurpose.LOGIN);

        // Consume-on-verify: replaying the same code must now fail
        when(otpCodeRepository.findFirstByEmailAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(
                "user@example.com", OtpPurpose.LOGIN)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class,
                () -> otpService.verify("user@example.com", code, OtpPurpose.LOGIN));

        assertTrue(otp.getVerified());
        assertTrue(otp.getInvalidated());
    }

    @Test
    void tooManyWrongAttemptsInvalidatesTheCode() {
        OtpCode otp = activeCode("123456");
        otp.setAttempts(5); // == maxAttempts
        when(otpCodeRepository.findFirstByEmailAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(
                "user@example.com", OtpPurpose.LOGIN)).thenReturn(Optional.of(otp));

        assertThrows(BusinessException.class,
                () -> otpService.verify("user@example.com", "999999", OtpPurpose.LOGIN));
        assertTrue(otp.getInvalidated());
    }

    @Test
    void expiredCodeIsRejectedAndInvalidated() {
        OtpCode otp = activeCode("123456");
        otp.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        when(otpCodeRepository.findFirstByEmailAndPurposeAndInvalidatedFalseOrderByCreatedAtDesc(
                "user@example.com", OtpPurpose.LOGIN)).thenReturn(Optional.of(otp));

        assertThrows(BusinessException.class,
                () -> otpService.verify("user@example.com", "123456", OtpPurpose.LOGIN));
        assertTrue(otp.getInvalidated());
        assertFalse(Boolean.TRUE.equals(otp.getVerified()));
    }

    @Test
    void sendOtpRejectsBlankIdentifier() {
        assertThrows(BusinessException.class,
                () -> otpService.sendOtp(" ", OtpPurpose.LOGIN));
        verify(otpCodeRepository, never()).save(any());
    }

    private static String sha256(String value) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(
                    digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
