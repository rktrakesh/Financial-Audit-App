package com.auditSystem.repository;

import com.auditSystem.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTransactionIdOrderByTimestampDesc(String transactionId);

    List<AuditLog> findByAccountIdOrderByTimestampDesc(String accountId);

    List<AuditLog> findBySeverity(AuditLog.AuditSeverity severity);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByAction(AuditLog.AuditAction action);
}
