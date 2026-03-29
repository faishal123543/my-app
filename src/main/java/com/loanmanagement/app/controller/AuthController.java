package com.loanmanagement.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.loanmanagement.app.entity.User;
import com.loanmanagement.app.entity.dto.AuthResponse;
import com.loanmanagement.app.entity.dto.LoginRequest;
import com.loanmanagement.app.entity.dto.OtpSendRequest;
import com.loanmanagement.app.entity.dto.OtpVerifyRequest;
import com.loanmanagement.app.entity.dto.RegisterRequest;
import com.loanmanagement.app.entity.dto.ResetPasswordRequest;
import com.loanmanagement.app.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/api/auth", "/api/v1/auth" })
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User saved = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(
                    "Registration successful. Account status: PENDING",
                    saved.getMobileNumber(),
                    saved.getId(),
                    saved.getName(),
                    saved.getNationalId(),
                    saved.getStatus()
                ));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<AuthResponse> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok().body(new AuthResponse("OTP sent successfully", request.getMobileNumber()));
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<AuthResponse> resendOtp(@Valid @RequestBody OtpSendRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok().body(new AuthResponse("OTP resent successfully", request.getMobileNumber()));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        User verifiedUser = authService.verifyOtp(request);
        return ResponseEntity.ok().body(new AuthResponse(
            "OTP verified. Account activated",
            verifiedUser.getMobileNumber(),
            verifiedUser.getId(),
            verifiedUser.getName(),
            verifiedUser.getNationalId(),
            verifiedUser.getStatus()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        return ResponseEntity.ok().body(new AuthResponse(
            "Log in successfully",
            user.getMobileNumber(),
            user.getId(),
            user.getName(),
            user.getNationalId(),
            user.getStatus()
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().body(new AuthResponse("Password reset successfully", request.getMobileNumber()));
    }
}
