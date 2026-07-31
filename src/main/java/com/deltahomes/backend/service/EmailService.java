package com.deltahomes.backend.service;

import com.deltahomes.backend.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails (OTP verification, etc.) via the configured SMTP
 * server. When no SMTP host is configured (local development), the OTP is
 * logged to the console instead so the flow can be tested end-to-end.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.from:noreply@deltahomes.app}")
    private String fromAddress;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        // Spring Boot only configures a JavaMailSender when spring.mail.host is set.
        // Fall back to a bare sender so the app still boots without SMTP in dev.
        this.mailSender = mailSenderProvider.getIfAvailable(JavaMailSenderImpl::new);
    }

    public void sendOtp(String email, String code) {
        send(email, "Delta Homes verification code",
                "Your Delta Homes verification code is: " + code + ". It is valid for a few minutes.");
    }

    public void send(String to, String subject, String body) {
        if (mailHost.isBlank()) {
            log.info("[DEV MODE] Email OTP for {}: {}", to, extractCode(body));
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw new BusinessException("Failed to send email. Please try again.");
        }
    }

    private static String extractCode(String body) {
        int idx = body.indexOf("is: ");
        return idx >= 0 ? body.substring(idx + 4) : "(code not parsed)";
    }
}
