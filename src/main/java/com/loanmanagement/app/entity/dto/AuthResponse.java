package com.loanmanagement.app.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String message;
    private String mobileNumber;
    private Long userId;
    private String name;
    private String nationalId;
    private String status;

    public AuthResponse(String message, String mobileNumber) {
        this.message = message;
        this.mobileNumber = mobileNumber;
    }

    public AuthResponse(String message, String mobileNumber, Long userId, String name, String nationalId, String status) {
        this.message = message;
        this.mobileNumber = mobileNumber;
        this.userId = userId;
        this.name = name;
        this.nationalId = nationalId;
        this.status = status;
    }
}
