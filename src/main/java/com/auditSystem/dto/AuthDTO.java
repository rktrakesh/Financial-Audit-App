package com.auditSystem.dto;

import com.auditSystem.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String username;
        private String role;
        private String token;
        private String tokenType = "Bearer";

        public LoginResponse(String username, String role, String token) {
            this.username = username;
            this.role = role;
            this.token = token;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private User.Role role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterResponse {
        private String username;
        private String role;
        private String token;
    }
}
