package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.AuthDtos;
import com.zharkyn.aiassistant_backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<AuthDtos.JwtAuthenticationResponse> signup(@Valid @RequestBody AuthDtos.SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signup(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthDtos.JwtAuthenticationResponse> signin(@Valid @RequestBody AuthDtos.SignInRequest request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }
}
