package com.auditSystem.service;

import com.auditSystem.model.AuditLog;
import com.auditSystem.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog log(String transactionId, String accountId,
                        AuditLog.AuditAction action, String details,
                        AuditLog.AuditSeverity severity, String performedBy,
                        String ipAddress) {
        log.debug("log::Creating audit log - action: {}, severity: {}", action, severity);
        AuditLog auditLog = AuditLog.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .action(action)
                .details(details)
                .severity(severity)
                .performedBy(performedBy != null ? performedBy : "SYSTEM")
                .ipAddress(ipAddress)
                .build();
        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("log::Audit logged: [{}] {} - {}", severity, action, details);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAllPaged(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getByTransaction(String transactionId) {
        return auditLogRepository.findByTransactionIdOrderByTimestampDesc(transactionId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getByAccount(String accountId) {
        return auditLogRepository.findByAccountIdOrderByTimestampDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getBySeverity(AuditLog.AuditSeverity severity) {
        return auditLogRepository.findBySeverity(severity);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }
}
