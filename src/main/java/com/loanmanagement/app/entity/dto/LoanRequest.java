package com.loanmanagement.app.entity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    @NotBlank(message = "Loan type is required")
    private String loanType; // PERSONAL, CAR, HOME

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Amount must be at least 1000")
    private BigDecimal amount;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    @Max(value = 360, message = "Duration cannot exceed 360 months (30 years)")
    private Integer durationMonths;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "1", message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;

    @NotBlank(message = "Employer name is required")
    private String employerName;

    @NotNull(message = "Please specify if you have existing loans")
    private Boolean hasExistingLoans;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 850, message = "Credit score cannot exceed 850")
    private Integer creditScore;

    @NotBlank(message = "Purpose of loan is required")
    private String purposeOfLoan;
}
