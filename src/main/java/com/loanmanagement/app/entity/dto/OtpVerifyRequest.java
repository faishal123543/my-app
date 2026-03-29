package com.loanmanagement.app.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpVerifyRequest {

    @NotNull(message = "Mobile number is required")
    @Pattern(regexp = "\\+?[0-9]{10,15}", message = "Mobile number must be 10 to 15 digits, optional leading +")
    private String mobileNumber;

    @NotNull(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
    private String otp;
}
