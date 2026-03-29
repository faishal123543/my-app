package com.loanmanagement.app.service;

import com.loanmanagement.app.entity.User;
import com.loanmanagement.app.entity.dto.ChangePasswordRequest;
import com.loanmanagement.app.entity.dto.UserProfileRequest;
import com.loanmanagement.app.entity.dto.UserProfileResponse;
import com.loanmanagement.app.exception.InvalidCredentialsException;
import com.loanmanagement.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Value("${app.upload.dir:./uploads/profile-photos}")
    private String uploadDir;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024; // 2 MB
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

    // ─── Get Profile ──────────────────────────────────────────────────────────

    public UserProfileResponse getUserProfile(Long userId) {
        return mapToResponse(findById(userId));
    }

    // ─── Update Profile ───────────────────────────────────────────────────────

    public UserProfileResponse updateProfile(Long userId, UserProfileRequest req) {
        User user = findById(userId);
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setAddress(req.getAddress());
        user.setEmploymentStatus(req.getEmploymentStatus());
        user.setMonthlyIncome(req.getMonthlyIncome());
        return mapToResponse(userRepository.save(user));
    }

    // ─── Upload Profile Photo ─────────────────────────────────────────────────

    public String uploadProfilePhoto(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 2 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG and PNG images are allowed");
        }

        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String ext = "image/png".equals(contentType) ? ".png" : ".jpg";
            String opposite = ".png".equals(ext) ? ".jpg" : ".png";

            // Remove photo with opposite extension if present
            Files.deleteIfExists(dir.resolve("user_" + userId + opposite));

            Path dest = dir.resolve("user_" + userId + ext);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/api/v1/users/" + userId + "/profile-photo";
            User user = findById(userId);
            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);
            log.info("Profile photo saved for user {}", userId);
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to save profile photo for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to store profile photo", e);
        }
    }

    // ─── Get Profile Photo ────────────────────────────────────────────────────

    public Resource getProfilePhoto(Long userId) {
        User user = findById(userId);
        if (user.getProfileImageUrl() == null) {
            throw new IllegalArgumentException("No profile photo found for user " + userId);
        }
        Path dir = Paths.get(uploadDir);
        for (String ext : List.of(".jpg", ".png")) {
            Path path = dir.resolve("user_" + userId + ext);
            if (Files.exists(path)) {
                return new FileSystemResource(path.toFile());
            }
        }
        throw new IllegalArgumentException("Profile photo file not found on disk");
    }

    // ─── Change Password ──────────────────────────────────────────────────────

    public void changePassword(Long userId, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        User user = findById(userId);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must differ from the current password");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated for user {}", userId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    private UserProfileResponse mapToResponse(User user) {
        UserProfileResponse r = new UserProfileResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setMobileNumber(user.getMobileNumber());
        r.setEmail(user.getEmail());
        r.setNationalId(user.getNationalId());
        r.setDateOfBirth(user.getDateOfBirth());
        r.setAddress(user.getAddress());
        r.setEmploymentStatus(user.getEmploymentStatus());
        r.setMonthlyIncome(user.getMonthlyIncome());
        r.setProfileImageUrl(user.getProfileImageUrl());
        r.setStatus(user.getStatus());
        return r;
    }
}
