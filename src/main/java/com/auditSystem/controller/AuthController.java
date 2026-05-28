package com.auditSystem.controller;

import com.auditSystem.dto.AuthDTO;
import com.auditSystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final UserService userService;

    // POST /api/auth/login
    @PostMapping("/login")
    @Operation(
            summary = "User Login",
            description = "Authenticate user with username and password. Returns JWT token on success."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful - JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<AuthDTO.LoginResponse> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        log.info("login::Login attempt for user: {}", request.getUsername());
        return ResponseEntity.ok(userService.login(request));
    }

    // POST /api/auth/register
    @PostMapping("/register")
    @Operation(
            summary = "User Registration",
            description = "Register a new user account with username, password, and email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
            @ApiResponse(responseCode = "409", description = "Username already taken")
    })
    public ResponseEntity<AuthDTO.RegisterResponse> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request) {
        log.info("register::Registration attempt for user: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }
}
