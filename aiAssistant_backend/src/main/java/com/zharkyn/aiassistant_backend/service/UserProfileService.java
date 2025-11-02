package com.zharkyn.aiassistant_backend.service;

import com.google.cloud.Timestamp;
import com.zharkyn.aiassistant_backend.dto.UserProfileDtos;
import com.zharkyn.aiassistant_backend.model.User;
import com.zharkyn.aiassistant_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

/**
 * Сервис для управления профилем пользователя
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB

    /**
     * Получить профиль текущего пользователя
     */
    public UserProfileDtos.ProfileResponse getMyProfile() 
            throws ExecutionException, InterruptedException {
        User currentUser = getCurrentUser();
        
        return UserProfileDtos.ProfileResponse.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .phoneNumber(currentUser.getPhoneNumber())
                .bio(currentUser.getBio())
                .avatarBase64(currentUser.getAvatarBase64())
                .linkedInProfile(currentUser.getLinkedInProfile())
                .githubProfile(currentUser.getGithubProfile())
                .createdAt(currentUser.getCreatedAt() != null ? 
                          currentUser.getCreatedAt().toDate().toString() : null)
                .build();
    }

    /**
     * Обновить профиль
     */
    public UserProfileDtos.ProfileResponse updateProfile(
            UserProfileDtos.UpdateProfileRequest request) 
            throws ExecutionException, InterruptedException {
        
        User currentUser = getCurrentUser();
        
        // Обновляем поля
        if (request.getFirstName() != null) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            currentUser.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBio() != null) {
            currentUser.setBio(request.getBio());
        }
        if (request.getLinkedInProfile() != null) {
            currentUser.setLinkedInProfile(request.getLinkedInProfile());
        }
        if (request.getGithubProfile() != null) {
            currentUser.setGithubProfile(request.getGithubProfile());
        }
        
        currentUser.setUpdatedAt(Timestamp.now());
        userRepository.save(currentUser);
        
        log.info("Profile updated for user: {}", currentUser.getEmail());
        
        return getMyProfile();
    }

    /**
     * Загрузить аватар
     */
    public UserProfileDtos.AvatarResponse uploadAvatar(MultipartFile file) 
            throws ExecutionException, InterruptedException, IOException {
        
        User currentUser = getCurrentUser();
        
        // Валидация файла
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new RuntimeException("File size exceeds 2MB limit");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }
        
        // Конвертируем в Base64
        byte[] imageBytes = file.getBytes();
        String base64Avatar = Base64.getEncoder().encodeToString(imageBytes);
        String avatarDataUrl = String.format("data:%s;base64,%s", contentType, base64Avatar);
        
        // Сохраняем
        currentUser.setAvatarBase64(avatarDataUrl);
        currentUser.setUpdatedAt(Timestamp.now());
        userRepository.save(currentUser);
        
        log.info("Avatar uploaded for user: {}", currentUser.getEmail());
        
        return UserProfileDtos.AvatarResponse.builder()
                .avatarUrl(avatarDataUrl)
                .message("Avatar uploaded successfully")
                .build();
    }

    /**
     * Удалить аватар
     */
    public void deleteAvatar() throws ExecutionException, InterruptedException {
        User currentUser = getCurrentUser();
        
        currentUser.setAvatarBase64(null);
        currentUser.setUpdatedAt(Timestamp.now());
        userRepository.save(currentUser);
        
        log.info("Avatar deleted for user: {}", currentUser.getEmail());
    }

    /**
     * Изменить пароль
     */
    public void changePassword(UserProfileDtos.ChangePasswordRequest request) 
            throws ExecutionException, InterruptedException {
        
        User currentUser = getCurrentUser();
        
        // Проверяем старый пароль
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        
        // Устанавливаем новый пароль
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        currentUser.setUpdatedAt(Timestamp.now());
        userRepository.save(currentUser);
        
        log.info("Password changed for user: {}", currentUser.getEmail());
    }

    /**
     * Удалить аккаунт
     */
    public void deleteAccount() throws ExecutionException, InterruptedException {
        User currentUser = getCurrentUser();
        
        // TODO: Удалить все связанные данные (интервью, резюме, оценки)
        
        userRepository.deleteById(currentUser.getId());
        
        log.info("Account deleted for user: {}", currentUser.getEmail());
    }

    // === Helper methods ===

    private User getCurrentUser() throws ExecutionException, InterruptedException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }
}