package com.auditSystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false, length = 1000)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditSeverity severity;

    private String performedBy;

    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public enum AuditAction {
        TRANSACTION_CREATED,
        TRANSACTION_UPDATED,
        TRANSACTION_FLAGGED,
        TRANSACTION_BLOCKED,
        TRANSACTION_APPROVED,
        SUSPICIOUS_ACTIVITY_DETECTED,
        COMPLIANCE_VIOLATION,
        ALERT_GENERATED,
        SYSTEM_CHECK
    }

    public enum AuditSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
