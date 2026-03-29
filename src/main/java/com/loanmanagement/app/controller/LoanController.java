package com.loanmanagement.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loanmanagement.app.entity.dto.DashboardSummary;
import com.loanmanagement.app.entity.dto.LoanRequest;
import com.loanmanagement.app.entity.dto.LoanResponse;
import com.loanmanagement.app.service.LoanService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({ "/api/loans", "/api/v1/loans" })
public class LoanController {

    private static final Logger log = LoggerFactory.getLogger(LoanController.class);
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Apply for a new loan
     * POST /api/loans
     * 
     * Expected to receive userId from JWT token or session in production
     * For now, we'll extract from the authenticated context
     */
    @PostMapping
    public ResponseEntity<LoanResponse> applyLoan(
            @Valid @RequestBody LoanRequest request,
            @RequestParam(value = "userId", required = false) Long userId) {
        
        log.info("Loan application received. Type: {}, Amount: {}", request.getLoanType(), request.getAmount());
        
        // TODO: In production, extract userId from JWT/authenticated user context
        // For now, we'll require it as a query parameter
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            LoanResponse response = loanService.applyLoan(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Loan application failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get all loans for the current user
     * GET /api/loans?userId=123
     */
    @GetMapping
    public ResponseEntity<List<LoanResponse>> getUserLoans(
            @RequestParam(value = "userId", required = false) Long userId) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Fetching loans for user ID: {}", userId);
        List<LoanResponse> loans = loanService.getUserLoans(userId);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get dashboard summary for the current user
     * GET /api/loans/dashboard?userId=123
     * or
     * GET /api/loans/dashboard/summary?userId=123
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummary> getDashboardSummary(
            @RequestParam(value = "userId", required = false) Long userId) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Fetching dashboard summary for user ID: {}", userId);
        DashboardSummary summary = loanService.getDashboardSummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Alternative endpoint for dashboard summary
     * GET /api/loans/dashboard/summary
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummaryAlt(
            @RequestParam(value = "userId", required = false) Long userId) {
        return getDashboardSummary(userId);
    }

    /**
     * Get a specific loan by ID
     * GET /api/loans/{loanId}
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoan(
            @PathVariable Long loanId,
            @RequestParam(value = "userId", required = false) Long userId) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Fetching loan ID: {} for user ID: {}", loanId, userId);
        LoanResponse loan = loanService.getLoan(userId, loanId);
        return ResponseEntity.ok(loan);
    }

    /**
     * Update loan status (Admin only)
     * PUT /api/loans/{loanId}/status
     */
    @PutMapping("/{loanId}/status")
    public ResponseEntity<LoanResponse> updateLoanStatus(
            @PathVariable Long loanId,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason) {
        
        log.info("Updating loan ID: {} to status: {}", loanId, status);
        LoanResponse updatedLoan = loanService.updateLoanStatus(loanId, status, rejectionReason);
        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Loan Service");
        return ResponseEntity.ok(response);
    }

    /**
     * Get loan statistics (for admin dashboard)
     * GET /api/loans/admin/stats
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "Admin stats endpoint - implement based on requirements");
        return ResponseEntity.ok(stats);
    }
}
