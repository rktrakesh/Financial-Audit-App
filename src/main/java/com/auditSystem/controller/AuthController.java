package com.auditSystem.controller;

import com.auditSystem.dto.AuthDTO;
import com.auditSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.LoginResponse> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        log.info("login::Login attempt for user: {}", request.getUsername());
        return ResponseEntity.ok(userService.login(request));
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthDTO.RegisterResponse> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request) {
        log.info("register::Registration attempt for user: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }
}
