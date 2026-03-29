package com.loanmanagement.app.entity.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProfileResponse {
    private Long id;
    private String name;
    private String mobileNumber;
    private String email;
    private String nationalId;
    private String dateOfBirth;
    private String address;
    private String employmentStatus;
    private BigDecimal monthlyIncome;
    private String profileImageUrl;
    private String status;
}
