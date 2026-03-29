package com.loanmanagement.app.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Production SMS service — active only when twilio.enabled=true.
 *
 * Required application.properties keys (when enabled):
 *   twilio.account-sid   — Your Twilio Account SID
 *   twilio.auth-token    — Your Twilio Auth Token
 *   twilio.from-number   — Your Twilio sender number in E.164 format, e.g. +12025551234
 */
@Service
@ConditionalOnProperty(name = "twilio.enabled", havingValue = "true")
public class TwilioSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsService.class);

    private final String fromNumber;

    public TwilioSmsService(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.from-number}") String fromNumber) {
        this.fromNumber = fromNumber;
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS service initialised. Sender: {}", fromNumber);
    }

    @Override
    public void sendOtp(String toPhoneNumber, String otpCode) {
        try {
            String body = "Your verification code is: " + otpCode
                    + ". Valid for 5 minutes. Do not share this code.";

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromNumber),
                    body
            ).create();

            log.info("OTP SMS dispatched to {}. Twilio SID: {}", toPhoneNumber, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP. Please try again later.", e);
        }
    }
}
