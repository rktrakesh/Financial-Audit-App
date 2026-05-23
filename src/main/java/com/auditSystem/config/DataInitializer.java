package com.auditSystem.config;

import com.auditSystem.model.User;
import com.auditSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfNotExists("admin", "admin123", User.Role.ADMIN);
        createUserIfNotExists("auditor", "auditor123", User.Role.AUDITOR);
        createUserIfNotExists("user", "user123", User.Role.USER);
    }

    private void createUserIfNotExists(String username, String password, User.Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("DataInitializer::Created default {} user: {}", role, username);
        }
    }
}
