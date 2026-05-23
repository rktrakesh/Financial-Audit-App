package com.auditSystem.service;

import com.auditSystem.dto.AuthDTO;
import com.auditSystem.exception.UnauthorizedException;
import com.auditSystem.model.User;
import com.auditSystem.repository.UserRepository;
import com.auditSystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthDTO.RegisterResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("register::New user registered: {} with role {}", user.getUsername(), user.getRole());

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthDTO.RegisterResponse(user.getUsername(), user.getRole().name(), token);
    }

    @Transactional(readOnly = true)
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("login::Failed login attempt for user: {}", request.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        log.info("login::User logged in: {} with role {}", user.getUsername(), user.getRole());
        return new AuthDTO.LoginResponse(user.getUsername(), user.getRole().name(), token);
    }
}
