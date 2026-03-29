package com.loanmanagement.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.loanmanagement.app.entity.Loan;
import com.loanmanagement.app.entity.User;
import com.loanmanagement.app.entity.dto.DashboardSummary;
import com.loanmanagement.app.entity.dto.LoanRequest;
import com.loanmanagement.app.entity.dto.LoanResponse;
import com.loanmanagement.app.exception.LoanApplicationException;
import com.loanmanagement.app.repository.LoanRepository;
import com.loanmanagement.app.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    // Business rule constants
    private static final int MINIMUM_CREDIT_SCORE = 600;
    private static final BigDecimal MINIMUM_MONTHLY_INCOME = new BigDecimal("3000");
    private static final int MAXIMUM_DEBT_TO_INCOME_RATIO = 50; // percent

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    /**
     * Apply for a new loan with validation
     */
    public LoanResponse applyLoan(Long userId, LoanRequest request) {
        log.info("Loan application received from user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LoanApplicationException("User not found"));

        // Validate loan request
        validateLoanRequest(request);

        // Apply business rules
        validateCreditScore(request);
        validateIncome(request);
        validateDebtToIncomeRatio(user, request);

        // Check for duplicate active loans of same type
        if (loanRepository.hasActiveLoanOfType(user, request.getLoanType())) {
            String message = "You already have an active " + request.getLoanType() + " loan";
            log.warn("Loan rejected: {}", message);
            throw new LoanApplicationException(message);
        }

        // Create and save loan
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setLoanType(request.getLoanType());
        loan.setAmount(request.getAmount());
        loan.setStatus("PENDING");
        loan.setDurationMonths(request.getDurationMonths());
        loan.setMonthlyIncome(request.getMonthlyIncome());
        loan.setEmployerName(request.getEmployerName());
        loan.setHasExistingLoans(request.getHasExistingLoans());
        loan.setCreditScore(request.getCreditScore());
        loan.setPurposeOfLoan(request.getPurposeOfLoan());

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan application submitted. Loan ID: {}, Amount: {}, Type: {}",
                savedLoan.getId(), savedLoan.getAmount(), savedLoan.getLoanType());

        return convertToResponse(savedLoan);
    }

    /**
     * Get all loans for a specific user
     */
    public List<LoanResponse> getUserLoans(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LoanApplicationException("User not found"));

        List<Loan> loans = loanRepository.findByUserOrderByCreatedAtDesc(user);
        return loans.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * Get dashboard summary for a user
     */
    public DashboardSummary getDashboardSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LoanApplicationException("User not found"));

        Long totalLoans = (long) loanRepository.findByUserOrderByCreatedAtDesc(user).size();
        Long approvedLoans = loanRepository.countByUserAndStatus(user, "APPROVED");
        Long rejectedLoans = loanRepository.countByUserAndStatus(user, "REJECTED");
        Long pendingLoans = loanRepository.countByUserAndStatus(user, "PENDING");

        BigDecimal totalAmount = loanRepository.sumTotalLoanAmountByUser(user);
        BigDecimal approvedAmount = loanRepository.getTotalApprovedAmountByUser(user);
        BigDecimal remainingBalance = approvedAmount; // In a real system, this would be based on payments

        DashboardSummary summary = new DashboardSummary();
        summary.setTotalLoansApplied(totalLoans);
        summary.setApprovedLoansCount(approvedLoans);
        summary.setRejectedLoansCount(rejectedLoans);
        summary.setPendingLoansCount(pendingLoans);
        summary.setTotalLoanAmountRequested(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        summary.setTotalApprovedAmount(approvedAmount != null ? approvedAmount : BigDecimal.ZERO);
        summary.setRemainingBalance(remainingBalance != null ? remainingBalance : BigDecimal.ZERO);

        // Add user profile info
        summary.setUserName(user.getName());
        summary.setMobileNumber(user.getMobileNumber());
        summary.setNationalId(user.getNationalId());
        summary.setAccountStatus(user.getStatus());

        log.info("Dashboard summary generated for user ID: {}", userId);
        return summary;
    }

    /**
     * Get a specific loan by ID (authorization check included)
     */
    public LoanResponse getLoan(Long userId, Long loanId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LoanApplicationException("User not found"));

        Loan loan = loanRepository.findByIdAndUser(loanId, user)
                .orElseThrow(() -> new LoanApplicationException("Loan not found or unauthorized access"));

        return convertToResponse(loan);
    }

    /**
     * Update loan status (admin function)
     */
    public LoanResponse updateLoanStatus(Long loanId, String status, String rejectionReason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanApplicationException("Loan not found"));

        if (!(status.equals("APPROVED") || status.equals("REJECTED") || status.equals("PENDING"))) {
            throw new LoanApplicationException("Invalid loan status");
        }

        loan.setStatus(status);
        if (status.equals("REJECTED") && rejectionReason != null) {
            loan.setRejectionReason(rejectionReason);
        }

        Loan updatedLoan = loanRepository.save(loan);
        log.info("Loan status updated. Loan ID: {}, New Status: {}", loanId, status);

        return convertToResponse(updatedLoan);
    }

    // ==================== VALIDATION METHODS ====================

    private void validateLoanRequest(LoanRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LoanApplicationException("Loan amount must be greater than 0");
        }

        if (request.getDurationMonths() == null || request.getDurationMonths() <= 0) {
            throw new LoanApplicationException("Loan duration must be at least 1 month");
        }

        if (request.getMonthlyIncome() == null || request.getMonthlyIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LoanApplicationException("Monthly income must be greater than 0");
        }

        if (request.getCreditScore() == null || request.getCreditScore() < 300 || request.getCreditScore() > 850) {
            throw new LoanApplicationException("Credit score must be between 300 and 850");
        }
    }

    private void validateCreditScore(LoanRequest request) {
        if (request.getCreditScore() < MINIMUM_CREDIT_SCORE) {
            String message = "Your credit score (" + request.getCreditScore()
                    + ") is below the minimum required (" + MINIMUM_CREDIT_SCORE + ")";
            log.warn("Loan rejected due to low credit score: {}", message);
            throw new LoanApplicationException(message);
        }
    }

    private void validateIncome(LoanRequest request) {
        if (request.getMonthlyIncome().compareTo(MINIMUM_MONTHLY_INCOME) < 0) {
            String message = "Your monthly income must be at least " + MINIMUM_MONTHLY_INCOME;
            log.warn("Loan rejected due to low income: {}", message);
            throw new LoanApplicationException(message);
        }
    }

    private void validateDebtToIncomeRatio(User user, LoanRequest request) {
        // Get total approved loan amount
        BigDecimal totalApprovedLoans = loanRepository.getTotalApprovedAmountByUser(user);
        if (totalApprovedLoans == null) {
            totalApprovedLoans = BigDecimal.ZERO;
        }

        // Calculate debt including new loan request
        BigDecimal totalDebt = totalApprovedLoans.add(request.getAmount());

        // Monthly payment calculation (simplified: total amount / duration months)
        BigDecimal monthlyPayment = totalDebt.divide(new BigDecimal(request.getDurationMonths()), 2, java.math.RoundingMode.HALF_UP);

        // Calculate debt-to-income ratio
        BigDecimal ratio = monthlyPayment.divide(request.getMonthlyIncome(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        if (ratio.intValue() > MAXIMUM_DEBT_TO_INCOME_RATIO) {
            String message = "Your debt-to-income ratio (" + ratio.intValue()
                    + "%) exceeds the maximum allowed (" + MAXIMUM_DEBT_TO_INCOME_RATIO + "%)";
            log.warn("Loan rejected due to high debt-to-income ratio: {}", message);
            throw new LoanApplicationException(message);
        }
    }

    // ==================== HELPER METHODS ====================

    private LoanResponse convertToResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setLoanType(loan.getLoanType());
        response.setAmount(loan.getAmount());
        response.setStatus(loan.getStatus());
        response.setDurationMonths(loan.getDurationMonths());
        response.setMonthlyIncome(loan.getMonthlyIncome());
        response.setEmployerName(loan.getEmployerName());
        response.setHasExistingLoans(loan.getHasExistingLoans());
        response.setCreditScore(loan.getCreditScore());
        response.setPurposeOfLoan(loan.getPurposeOfLoan());
        response.setCreatedAt(loan.getCreatedAt());
        response.setUpdatedAt(loan.getUpdatedAt());
        response.setRejectionReason(loan.getRejectionReason());
        return response;
    }
}
