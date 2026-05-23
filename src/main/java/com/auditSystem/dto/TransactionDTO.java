package com.auditSystem.dto;

import com.auditSystem.model.Alert;
import com.auditSystem.model.AuditLog;
import com.auditSystem.model.Transaction;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @NotBlank(message = "Account ID is required")
        private String accountId;

        @NotBlank(message = "Recipient account ID is required")
        private String recipientAccountId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
        private BigDecimal amount;

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g. USD)")
        private String currency;

        @NotNull(message = "Transaction type is required")
        private Transaction.TransactionType transactionType;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;

        private String ipAddress;
        private String location;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String transactionId;
        private String accountId;
        private String recipientAccountId;
        private BigDecimal amount;
        private String currency;
        private Transaction.TransactionType transactionType;
        private Transaction.TransactionStatus status;
        private LocalDateTime transactionDate;
        private String description;
        private LocalDateTime createdAt;
        private List<String> flagReasons;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuditResponse {
        private Long id;
        private String transactionId;
        private String accountId;
        private AuditLog.AuditAction action;
        private String details;
        private AuditLog.AuditSeverity severity;
        private String performedBy;
        private LocalDateTime timestamp;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AlertResponse {
        private Long id;
        private String transactionId;
        private String accountId;
        private Alert.AlertType alertType;
        private Alert.AlertStatus status;
        private String message;
        private Alert.AlertSeverity severity;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AlertResolutionRequest {
        @NotNull
        private Alert.AlertStatus status;
        private String resolvedBy;
        private String resolutionNote;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DashboardSummary {
        private long totalTransactions;
        private long flaggedTransactions;
        private long blockedTransactions;
        private long completedTransactions;
        private long openAlerts;
        private long criticalAlerts;
        private BigDecimal totalAmountProcessed;
    }
}
