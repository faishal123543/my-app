package com.loanmanagement.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loanmanagement.app.entity.OtpCode;
import com.loanmanagement.app.entity.User;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByUserOrderByCreatedAtDesc(User user);
}
