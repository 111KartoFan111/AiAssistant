package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.UserProfileDtos;
import com.zharkyn.aiassistant_backend.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ExecutionException;

/**
 * REST контроллер для профиля пользователя
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    /**
     * Получить свой профиль
     */
    @GetMapping
    public ResponseEntity<UserProfileDtos.ProfileResponse> getMyProfile() {
        try {
            log.info("Fetching user profile");
            UserProfileDtos.ProfileResponse profile = profileService.getMyProfile();
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error fetching profile", e);
            throw new RuntimeException("Failed to fetch profile: " + e.getMessage(), e);
        }
    }

    /**
     * Обновить профиль
     */
    @PutMapping
    public ResponseEntity<UserProfileDtos.ProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileDtos.UpdateProfileRequest request) {
        try {
            log.info("Updating user profile");
            UserProfileDtos.ProfileResponse profile = profileService.updateProfile(request);
            log.info("Profile updated successfully");
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error updating profile", e);
            throw new RuntimeException("Failed to update profile: " + e.getMessage(), e);
        }
    }

    /**
     * Загрузить аватар
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileDtos.AvatarResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading avatar, file size: {} bytes", file.getSize());
            UserProfileDtos.AvatarResponse response = profileService.uploadAvatar(file);
            log.info("Avatar uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading avatar", e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить аватар
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar() {
        try {
            log.info("Deleting avatar");
            profileService.deleteAvatar();
            log.info("Avatar deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting avatar", e);
            throw new RuntimeException("Failed to delete avatar: " + e.getMessage(), e);
        }
    }

    /**
     * Изменить пароль
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody UserProfileDtos.ChangePasswordRequest request) {
        try {
            log.info("Changing password");
            profileService.changePassword(request);
            log.info("Password changed successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error changing password", e);
            throw new RuntimeException("Failed to change password: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить аккаунт
     */
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount() {
        try {
            log.info("Deleting account");
            profileService.deleteAccount();
            log.info("Account deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting account", e);
            throw new RuntimeException("Failed to delete account: " + e.getMessage(), e);
        }
    }
}