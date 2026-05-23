package com.auditSystem.service;

import com.auditSystem.dto.TransactionDTO;
import com.auditSystem.exception.AlertNotFoundException;
import com.auditSystem.model.Alert;
import com.auditSystem.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional
    public Alert createAlert(String transactionId, String accountId,
                             Alert.AlertType type, Alert.AlertSeverity severity,
                             String message) {
        log.info("createAlert::Creating alert type {} for transaction {}", type, transactionId);
        Alert alert = Alert.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .alertType(type)
                .severity(severity)
                .message(message)
                .status(Alert.AlertStatus.OPEN)
                .build();
        Alert saved = alertRepository.save(alert);
        log.warn("createAlert::ALERT [{}][{}]: {} - {}", severity, type, transactionId, message);
        return saved;
    }

    @Transactional
    public Alert resolveAlert(Long alertId, TransactionDTO.AlertResolutionRequest request) {
        log.info("resolveAlert::Resolving alert {} with status {}", alertId, request.getStatus());
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));

        alert.setStatus(request.getStatus());
        alert.setResolvedBy(request.getResolvedBy());
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNote(request.getResolutionNote());

        Alert resolved = alertRepository.save(alert);
        log.info("resolveAlert::Alert {} successfully resolved by {}", alertId, request.getResolvedBy());
        return resolved;
    }

    @Transactional(readOnly = true)
    public List<Alert> getOpenAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(Alert.AlertStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public Page<Alert> getAllPaged(Pageable pageable) {
        return alertRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Alert> getByAccount(String accountId) {
        return alertRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public List<Alert> getBySeverity(Alert.AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }

    @Transactional(readOnly = true)
    public List<Alert> getByTransaction(String transactionId) {
        return alertRepository.findByTransactionId(transactionId);
    }

    @Transactional(readOnly = true)
    public long countOpenAlerts() {
        return alertRepository.countByStatus(Alert.AlertStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public long countCriticalAlerts() {
        // Fixed: was incorrectly counting OPEN instead of CRITICAL+OPEN
        return alertRepository.countBySeverityAndStatus(Alert.AlertSeverity.CRITICAL, Alert.AlertStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public List<Alert> getAll() {
        return alertRepository.findAll();
    }
}
