package com.loanmanagement.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Fallback SMS service — active when twilio.enabled=false or is not set.
 * Prints the OTP to the console (stdout + SLF4J WARN) so developers can
 * test the full OTP flow without a real SMS provider.
 *
 * NEVER use this in production.
 */
@Service
@ConditionalOnProperty(name = "twilio.enabled", havingValue = "false", matchIfMissing = true)
public class ConsoleSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleSmsService.class);

    @Override
    public void sendOtp(String toPhoneNumber, String otpCode) {
        log.warn("[DEV-ONLY] Twilio not configured. OTP for {} will be printed to console.", toPhoneNumber);
        System.out.println("==============================================");
        System.out.println("  [OTP - DEV MODE]");
        System.out.println("  To      : " + toPhoneNumber);
        System.out.println("  OTP Code: " + otpCode);
        System.out.println("  (Set twilio.enabled=true for real SMS)");
        System.out.println("==============================================");
    }
}
