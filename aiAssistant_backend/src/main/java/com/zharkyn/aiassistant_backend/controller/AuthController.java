package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.AuthDtos;
import com.zharkyn.aiassistant_backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<AuthDtos.JwtAuthenticationResponse> signup(@Valid @RequestBody AuthDtos.SignUpRequest request) {
        log.info("Signup request received for email: {}", request.getEmail());
        AuthDtos.JwtAuthenticationResponse response = authenticationService.signup(request);
        log.info("Signup successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthDtos.JwtAuthenticationResponse> signin(@Valid @RequestBody AuthDtos.SignInRequest request) {
        log.info("Signin request received for email: {}", request.getEmail());
        AuthDtos.JwtAuthenticationResponse response = authenticationService.signin(request);
        log.info("Signin successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("Test endpoint called");
        return ResponseEntity.ok("Auth controller is working!");
    }
}