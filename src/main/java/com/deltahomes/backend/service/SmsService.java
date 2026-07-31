package com.deltahomes.backend.service;

import com.deltahomes.backend.exception.BusinessException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Sends SMS messages via Twilio. When Twilio credentials are not configured
 * (local development), the OTP is logged to the console instead so the flow
 * can be tested end-to-end without spending real SMS credits.
 */
@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromNumber;

    public void sendOtp(String phone, String code) {
        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            log.info("[DEV MODE] SMS OTP for {}: {}", phone, code);
            return;
        }
        try {
            Twilio.init(accountSid, authToken);
            Message.creator(new PhoneNumber(phone), new PhoneNumber(fromNumber),
                    "Delta Homes verification code: " + code + ". Valid for 5 minutes.").create();
        } catch (Exception e) {
            log.error("Failed to send SMS to {}", phone, e);
            throw new BusinessException("Failed to send SMS. Please try again.");
        }
    }
}
