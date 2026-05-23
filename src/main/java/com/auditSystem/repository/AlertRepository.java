package com.auditSystem.repository;

import com.auditSystem.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatusOrderByCreatedAtDesc(Alert.AlertStatus status);
    List<Alert> findByAccountId(String accountId);
    List<Alert> findBySeverity(Alert.AlertSeverity severity);
    List<Alert> findByTransactionId(String transactionId);

    long countByStatus(Alert.AlertStatus status);
    long countBySeverityAndStatus(Alert.AlertSeverity severity, Alert.AlertStatus status);
}
