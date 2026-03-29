package com.loanmanagement.app.controller;

import com.loanmanagement.app.entity.dto.ChangePasswordRequest;
import com.loanmanagement.app.entity.dto.UserProfileRequest;
import com.loanmanagement.app.entity.dto.UserProfileResponse;
import com.loanmanagement.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping({"/api/users", "/api/v1/users"})
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201", "http://localhost:4202"})
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/v1/users/{id}
     * Fetch full profile of a user.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    /**
     * PUT /api/v1/users/{id}
     * Update editable profile fields.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    /**
     * POST /api/v1/users/{id}/upload-profile-photo
     * Upload or replace profile photo (multipart/form-data, field: "file").
     */
    @PostMapping("/{id}/upload-profile-photo")
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = userService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(Map.of("profileImageUrl", imageUrl));
    }

    /**
     * GET /api/v1/users/{id}/profile-photo
     * Serve the stored profile photo as an image resource.
     */
    @GetMapping("/{id}/profile-photo")
    public ResponseEntity<Resource> getProfilePhoto(@PathVariable Long id) {
        Resource resource = userService.getProfilePhoto(id);
        String filename = resource.getFilename() != null ? resource.getFilename() : "";
        MediaType mediaType = filename.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    /**
     * PUT /api/v1/users/{id}/change-password
     * Update password after verifying the current one.
     */
    @PutMapping("/{id}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
