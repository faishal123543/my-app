package com.loanmanagement.app.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private Long id;
    private String loanType;
    private BigDecimal amount;
    private String status;
    private Integer durationMonths;
    private BigDecimal monthlyIncome;
    private String employerName;
    private Boolean hasExistingLoans;
    private Integer creditScore;
    private String purposeOfLoan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String rejectionReason;
}
