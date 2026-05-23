package com.auditSystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    private String resolvedBy;

    private LocalDateTime resolvedAt;

    private String resolutionNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = AlertStatus.OPEN;
    }

    public enum AlertType {
        LARGE_TRANSACTION,
        RAPID_TRANSACTIONS,
        UNUSUAL_HOURS,
        SUSPICIOUS_PATTERN,
        COMPLIANCE_BREACH,
        DUPLICATE_TRANSACTION,
        BLACKLISTED_ACCOUNT
    }

    public enum AlertStatus {
        OPEN, UNDER_REVIEW, RESOLVED, DISMISSED
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
