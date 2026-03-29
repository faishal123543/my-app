package com.loanmanagement.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loanmanagement.app.entity.Loan;
import com.loanmanagement.app.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Find all loans for a specific user
    List<Loan> findByUserOrderByCreatedAtDesc(User user);

    // Find active loans for a user (not rejected)
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.status IN ('PENDING', 'APPROVED') ORDER BY l.createdAt DESC")
    List<Loan> findActiveLoansByUser(@Param("user") User user);

    // Count loans by status for a user
    Long countByUserAndStatus(User user, String status);

    // Check if user has an active loan of a specific type
    @Query("SELECT COUNT(l) > 0 FROM Loan l WHERE l.user = :user AND l.loanType = :loanType AND l.status IN ('PENDING', 'APPROVED')")
    boolean hasActiveLoanOfType(@Param("user") User user, @Param("loanType") String loanType);

    // Get sum of approved loan amounts for a user
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM Loan l WHERE l.user = :user AND l.status = 'APPROVED'")
    BigDecimal sumApprovedLoanAmountByUser(@Param("user") User user);

    // Get sum of total loan amounts requested by a user
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM Loan l WHERE l.user = :user")
    BigDecimal sumTotalLoanAmountByUser(@Param("user") User user);

    // Get total approved amount for a user
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM Loan l WHERE l.user = :user AND l.status = 'APPROVED'")
    BigDecimal getTotalApprovedAmountByUser(@Param("user") User user);

    // Find loan by id and user (for authorization)
    Optional<Loan> findByIdAndUser(Long loanId, User user);

    // Count loans by type for a user
    Long countByUserAndLoanType(User user, String loanType);
}
