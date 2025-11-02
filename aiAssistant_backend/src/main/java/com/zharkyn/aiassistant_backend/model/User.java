package com.zharkyn.aiassistant_backend.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Модель пользователя с профильными полями
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    private String id;
    private String email;
    private String password;
    
    // Профильные поля
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String bio; // О себе
    private String avatarBase64; // Аватар в base64
    private String linkedInProfile;
    private String githubProfile;
    
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Реализация UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}