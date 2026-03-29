package com.loanmanagement.app.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.loanmanagement.app.entity.OtpCode;
import com.loanmanagement.app.entity.User;
import com.loanmanagement.app.entity.dto.LoginRequest;
import com.loanmanagement.app.entity.dto.OtpSendRequest;
import com.loanmanagement.app.entity.dto.OtpVerifyRequest;
import com.loanmanagement.app.entity.dto.RegisterRequest;
import com.loanmanagement.app.entity.dto.ResetPasswordRequest;
import com.loanmanagement.app.exception.InvalidCredentialsException;
import com.loanmanagement.app.exception.UserAlreadyExistsException;
import com.loanmanagement.app.repository.OtpCodeRepository;
import com.loanmanagement.app.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final Pattern SAUDI_MOBILE_PATTERN = Pattern.compile("^05\\d{8}$");
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final String USER_STATUS_PENDING = "PENDING";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final SmsService smsService;

    public AuthService(UserRepository userRepository, OtpCodeRepository otpCodeRepository, SmsService smsService) {
        this.userRepository = userRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.smsService = smsService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secureRandom = new SecureRandom();
    }

    public User register(RegisterRequest request) {
        if (request == null
                || isBlank(request.getMobileNumber())
                || isBlank(request.getPassword())
                || isBlank(request.getName())
                || isBlank(request.getNationalId())) {
            throw new IllegalArgumentException("mobileNumber, name, nationalId, and password are required");
        }

        String mobileNumber = normalizeSaudiMobile(request.getMobileNumber());
        if (!SAUDI_MOBILE_PATTERN.matcher(mobileNumber).matches()) {
            throw new IllegalArgumentException("Invalid Saudi mobile number; expected format 05xxxxxxxx");
        }

        if (request.getName().length() < 2 || request.getName().length() > 100) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters");
        }

        if (request.getNationalId().length() < 8 || request.getNationalId().length() > 30) {
            throw new IllegalArgumentException("National ID must be between 8 and 30 characters");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new UserAlreadyExistsException("User with this mobile number already exists");
        }

        log.info("Registering new user with mobile: {}", mobileNumber);
        User user = new User();
        user.setMobileNumber(mobileNumber);
        user.setName(request.getName().trim());
        user.setNationalId(request.getNationalId().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);
        user.setStatus(USER_STATUS_PENDING);

        return userRepository.save(user);
    }

    public void sendOtp(OtpSendRequest request) {
        if (request == null || isBlank(request.getMobileNumber())) {
            throw new IllegalArgumentException("mobileNumber is required");
        }

        log.info("OTP send requested for mobile: {}", request.getMobileNumber());
        User user = findFirstUserByMobile(request.getMobileNumber());
        createAndStoreOtp(user);
    }

    public void resendOtp(OtpSendRequest request) {
        sendOtp(request);
    }

    public User verifyOtp(OtpVerifyRequest request) {
        if (request == null || isBlank(request.getMobileNumber()) || isBlank(request.getOtp())) {
            throw new IllegalArgumentException("mobileNumber and otp are required");
        }

        User user = findFirstUserByMobile(request.getMobileNumber());
        OtpCode otpCode = otpCodeRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found. Please request a new OTP"));

        if (otpCode.isVerified()) {
            throw new IllegalArgumentException("OTP already used. Please request a new OTP");
        }

        if (otpCode.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            throw new IllegalArgumentException("OTP attempts exceeded. Please request a new OTP");
        }

        if (LocalDateTime.now().isAfter(otpCode.getExpiresAt())) {
            throw new IllegalArgumentException("OTP expired. Please request a new OTP");
        }

        if (!otpCode.getCode().equals(request.getOtp().trim())) {
            otpCode.setAttemptCount(otpCode.getAttemptCount() + 1);
            otpCodeRepository.save(otpCode);
            throw new IllegalArgumentException("Invalid OTP");
        }

        otpCode.setVerified(true);
        otpCodeRepository.save(otpCode);

        user.setVerified(true);
        user.setStatus(USER_STATUS_ACTIVE);
        User activatedUser = userRepository.save(user);
        log.info("Account activated for mobile: {}", activatedUser.getMobileNumber());
        return activatedUser;
    }

    public User login(LoginRequest request) {
        if (request == null || isBlank(request.getMobileNumber()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("mobileNumber and password are required");
        }

        List<User> candidates = findUsersByAnyMobileFormat(request.getMobileNumber());
        if (candidates.isEmpty()) {
            throw new InvalidCredentialsException("Invalid mobile number or password");
        }

        User authenticatedUser = null;
        for (User candidate : candidates) {
            if (matchesPassword(candidate, request.getPassword())) {
                authenticatedUser = candidate;
                break;
            }
        }

        if (authenticatedUser == null) {
            throw new InvalidCredentialsException("Invalid mobile number or password");
        }

        if (!authenticatedUser.isVerified()) {
            throw new InvalidCredentialsException("User account is pending verification");
        }

        if (authenticatedUser.getStatus() != null
                && !USER_STATUS_ACTIVE.equalsIgnoreCase(authenticatedUser.getStatus())) {
            throw new InvalidCredentialsException("User account is pending verification");
        }

        log.info("Login successful for mobile: {}", authenticatedUser.getMobileNumber());
        return authenticatedUser;
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (request == null || isBlank(request.getMobileNumber()) || isBlank(request.getNewPassword())) {
            throw new IllegalArgumentException("mobileNumber and newPassword are required");
        }

        if (request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("newPassword must be at least 8 characters");
        }

        List<User> candidates = findUsersByAnyMobileFormat(request.getMobileNumber());
        if (candidates.isEmpty()) {
            throw new InvalidCredentialsException("User not found for provided mobile number");
        }

        User user = candidates.get(0);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private void createAndStoreOtp(User user) {
        String otp = generateSixDigitOtp();

        OtpCode otpCode = new OtpCode();
        otpCode.setUser(user);
        otpCode.setCode(otp);
        otpCode.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpCode.setAttemptCount(0);
        otpCode.setVerified(false);
        otpCode.setCreatedAt(LocalDateTime.now());
        otpCodeRepository.save(otpCode);

        log.debug("OTP persisted for user {} (expires {})", user.getMobileNumber(), otpCode.getExpiresAt());

        String internationalNumber = toInternationalFormat(user.getMobileNumber());
        smsService.sendOtp(internationalNumber, otp);
    }

    private String generateSixDigitOtp() {
        int value = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(value);
    }

    private User findFirstUserByMobile(String mobileNumber) {
        List<User> candidates = findUsersByAnyMobileFormat(mobileNumber);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("User not found for provided mobile number");
        }
        return candidates.get(0);
    }

    private List<User> findUsersByAnyMobileFormat(String inputMobileNumber) {
        List<User> matches = new ArrayList<>();
        Set<Long> seenIds = new LinkedHashSet<>();

        for (String candidate : buildMobileCandidates(inputMobileNumber)) {
            Optional<User> user = userRepository.findByMobileNumber(candidate);
            if (user.isPresent() && user.get().getId() != null && seenIds.add(user.get().getId())) {
                matches.add(user.get());
            }
        }

        String localSuffix = extractLocalNineDigits(inputMobileNumber);
        if (!isBlank(localSuffix)) {
            List<User> suffixMatches = userRepository.findByMobileNumberEndingWith(localSuffix);
            for (User user : suffixMatches) {
                if (user.getId() != null && seenIds.add(user.getId())) {
                    matches.add(user);
                }
            }
        }

        return matches;
    }

    private List<String> buildMobileCandidates(String inputMobileNumber) {
        String trimmed = inputMobileNumber == null ? null : inputMobileNumber.trim();
        String normalized = normalizeSaudiMobile(trimmed);

        Set<String> candidates = new LinkedHashSet<>();

        if (!isBlank(trimmed)) {
            candidates.add(trimmed);
        }
        if (!isBlank(normalized)) {
            candidates.add(normalized);
        }

        if (!isBlank(normalized) && normalized.startsWith("05") && normalized.length() == 10) {
            String withoutLeadingZero = normalized.substring(1);
            candidates.add("+966" + withoutLeadingZero);
            candidates.add("966" + withoutLeadingZero);
            candidates.add("00966" + withoutLeadingZero);
        }

        return new ArrayList<>(candidates);
    }

    private boolean matchesPassword(User user, String rawPassword) {
        String storedPassword = user.getPassword();
        if (isBlank(storedPassword)) {
            return false;
        }

        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        // Backward compatibility for legacy plaintext passwords.
        if (storedPassword.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }

    private boolean isBcryptHash(String value) {
        return value != null && value.startsWith("$2");
    }

    private String extractLocalNineDigits(String mobileNumber) {
        if (mobileNumber == null) {
            return null;
        }

        String digits = mobileNumber.replaceAll("\\D", "");
        if (digits.length() < 9) {
            return null;
        }

        return digits.substring(digits.length() - 9);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeSaudiMobile(String mobileNumber) {
        if (mobileNumber == null) {
            return null;
        }

        String normalized = mobileNumber.trim();

        if (normalized.startsWith("+966") && normalized.length() >= 13) {
            normalized = "0" + normalized.substring(4);
        } else if (normalized.startsWith("00966") && normalized.length() >= 14) {
            normalized = "0" + normalized.substring(5);
        } else if (normalized.startsWith("966") && normalized.length() >= 12) {
            normalized = "0" + normalized.substring(3);
        }

        return normalized;
    }

    /**
     * Converts a Saudi local mobile number (05xxxxxxxx) to E.164 international
     * format (+966xxxxxxxxx) required by SMS gateways.
     * If the number is already in international format, it is returned as-is.
     */
    private String toInternationalFormat(String mobileNumber) {
        String normalized = normalizeSaudiMobile(mobileNumber);
        if (normalized != null && normalized.startsWith("05") && normalized.length() == 10) {
            // "05xxxxxxxx" → "+9665xxxxxxxx"
            return "+966" + normalized.substring(1);
        }
        return mobileNumber;
    }
}
