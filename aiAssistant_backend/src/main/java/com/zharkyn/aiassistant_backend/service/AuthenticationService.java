package com.zharkyn.aiassistant_backend.service;

import com.zharkyn.aiassistant_backend.dto.AuthDtos;
import com.zharkyn.aiassistant_backend.model.User;
import com.zharkyn.aiassistant_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDtos.JwtAuthenticationResponse signup(AuthDtos.SignUpRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already in use.");
            }
            var user = User.builder()
                    .firstName(request.getFullName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();
            userRepository.save(user);
            var jwt = jwtService.generateToken(user);
            return AuthDtos.JwtAuthenticationResponse.builder().token(jwt).build();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error during signup: " + e.getMessage());
        }
    }

    public AuthDtos.JwtAuthenticationResponse signin(AuthDtos.SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        try {
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
            var jwt = jwtService.generateToken(user);
            return AuthDtos.JwtAuthenticationResponse.builder().token(jwt).build();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error during signin: " + e.getMessage());
        }
    }
}
