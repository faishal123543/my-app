package com.loanmanagement.app.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {

    private Long totalLoansApplied;
    private Long approvedLoansCount;
    private Long rejectedLoansCount;
    private Long pendingLoansCount;
    private BigDecimal totalLoanAmountRequested;
    private BigDecimal totalApprovedAmount;
    private BigDecimal remainingBalance;
    
    // User profile
    private String userName;
    private String mobileNumber;
    private String nationalId;
    private String accountStatus;
}
