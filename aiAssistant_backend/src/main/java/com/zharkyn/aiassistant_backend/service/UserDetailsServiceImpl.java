package com.zharkyn.aiassistant_backend.service;

import com.zharkyn.aiassistant_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 * Этот сервис отвечает за загрузку данных пользователя для Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // В нашем приложении "username" - это email пользователя.
        try {
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        } catch (ExecutionException | InterruptedException e) {
            // Прерываем поток, если он был прерван во время ожидания
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // Оборачиваем ошибку Firestore в RuntimeException, чтобы Spring мог ее обработать
            throw new RuntimeException("Error fetching user data from Firestore", e);
        }
    }
}