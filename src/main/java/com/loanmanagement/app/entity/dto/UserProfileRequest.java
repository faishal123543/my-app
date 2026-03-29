package com.loanmanagement.app.entity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private String dateOfBirth;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private String employmentStatus;

    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly income must be a positive amount")
    private BigDecimal monthlyIncome;
}
