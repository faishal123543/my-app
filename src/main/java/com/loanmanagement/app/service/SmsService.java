package com.loanmanagement.app.service;

/**
 * Abstraction for OTP SMS delivery.
 * Two implementations are provided:
 *  - ConsoleSmsService  (active when twilio.enabled=false or not set — dev/test mode)
 *  - TwilioSmsService   (active when twilio.enabled=true — production)
 */
public interface SmsService {

    /**
     * Send a one-time password to the given phone number.
     *
     * @param toPhoneNumber  E.164 international format, e.g. "+966570980122"
     * @param otpCode        The six-digit OTP string
     */
    void sendOtp(String toPhoneNumber, String otpCode);
}
