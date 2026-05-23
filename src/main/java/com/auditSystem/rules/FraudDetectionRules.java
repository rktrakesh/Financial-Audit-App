package com.auditSystem.rules;

import com.auditSystem.model.Alert;
import com.auditSystem.model.Transaction;
import com.auditSystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionRules {

    private final TransactionRepository transactionRepository;

    @Value("${app.fraud.large-transaction-threshold:10000.00}")
    private BigDecimal largeTransactionThreshold;

    @Value("${app.fraud.rapid-transaction-limit:5}")
    private int rapidTransactionLimit;

    @Value("${app.fraud.rapid-transaction-window-minutes:10}")
    private int rapidTransactionWindowMinutes;

    @Value("${app.fraud.suspicious-hour-start:0}")
    private int suspiciousHourStart;

    @Value("${app.fraud.suspicious-hour-end:5}")
    private int suspiciousHourEnd;

    /**
     * Runs all fraud detection rules against a transaction.
     * Returns a list of FlagResult for each triggered rule.
     */
    public List<FlagResult> evaluate(Transaction transaction) {
        try {
            log.info("evaluate::Running fraud detection for transaction {}", transaction.getTransactionId());
            List<FlagResult> results = new ArrayList<>();

            checkLargeTransaction(transaction, results);
            checkRapidTransactions(transaction, results);
            checkUnusualHours(transaction, results);
            checkDuplicateTransaction(transaction, results);

            if (!results.isEmpty()) {
                log.warn("evaluate::Fraud detection found {} flags for transaction {}",
                        results.size(), transaction.getTransactionId());
            } else {
                log.info("evaluate::No fraud flags detected for transaction {}", transaction.getTransactionId());
            }
            return results;
        } catch (Exception ex) {
            log.error("evaluate::Error evaluating fraud detection for transaction {} - {}",
                    transaction.getTransactionId(), ex.getMessage(), ex);
            throw new RuntimeException("Error evaluating fraud detection: " + ex.getMessage(), ex);
        }
    }

    // ── Rule 1: Large Transaction

    private void checkLargeTransaction(Transaction t, List<FlagResult> results) {
        try {
            if (t.getAmount().compareTo(largeTransactionThreshold) >= 0) {
                String message = String.format(
                        "Large transaction detected: %s %s exceeds threshold of %s",
                        t.getAmount(), t.getCurrency(), largeTransactionThreshold);
                log.warn("checkLargeTransaction::{}", message);

                results.add(FlagResult.builder()
                        .alertType(Alert.AlertType.LARGE_TRANSACTION)
                        .severity(t.getAmount().compareTo(new BigDecimal("50000")) >= 0
                                ? Alert.AlertSeverity.CRITICAL : Alert.AlertSeverity.HIGH)
                        .message(message)
                        .build());
            }
        } catch (Exception ex) {
            log.error("checkLargeTransaction::Error checking large transaction for {} - {}",
                    t.getTransactionId(), ex.getMessage(), ex);
        }
    }

    // ── Rule 2: Rapid Transactions

    private void checkRapidTransactions(Transaction t, List<FlagResult> results) {
        try {
            LocalDateTime windowStart = LocalDateTime.now()
                    .minusMinutes(rapidTransactionWindowMinutes);
            long recentCount = transactionRepository
                    .countByAccountIdAndTransactionDateAfter(t.getAccountId(), windowStart);

            if (recentCount >= rapidTransactionLimit) {
                String message = String.format(
                        "Rapid transactions detected: %d transactions from account %s in %d minutes",
                        recentCount + 1, t.getAccountId(), rapidTransactionWindowMinutes);
                log.warn("checkRapidTransactions::{}", message);

                results.add(FlagResult.builder()
                        .alertType(Alert.AlertType.RAPID_TRANSACTIONS)
                        .severity(Alert.AlertSeverity.HIGH)
                        .message(message)
                        .build());
            }
        } catch (Exception ex) {
            log.error("checkRapidTransactions::Error checking rapid transactions for {} - {}",
                    t.getTransactionId(), ex.getMessage(), ex);
        }
    }

    // ── Rule 3: Unusual Hours

    private void checkUnusualHours(Transaction t, List<FlagResult> results) {
        try {
            int hour = t.getTransactionDate().getHour();
            if (hour >= suspiciousHourStart && hour < suspiciousHourEnd) {
                String message = String.format(
                        "Transaction at unusual hour: %02d:00 from account %s",
                        hour, t.getAccountId());
                log.warn("checkUnusualHours::{}", message);

                results.add(FlagResult.builder()
                        .alertType(Alert.AlertType.UNUSUAL_HOURS)
                        .severity(Alert.AlertSeverity.MEDIUM)
                        .message(message)
                        .build());
            }
        } catch (Exception ex) {
            log.error("checkUnusualHours::Error checking unusual hours for {} - {}",
                    t.getTransactionId(), ex.getMessage(), ex);
        }
    }

    // ── Rule 4: Duplicate Transaction

    private void checkDuplicateTransaction(Transaction t, List<FlagResult> results) {
        try {
            LocalDateTime windowStart = LocalDateTime.now().minusMinutes(5);
            List<Transaction> recent = transactionRepository
                    .findRecentByAccount(t.getAccountId(), windowStart);

            boolean duplicate = recent.stream().anyMatch(existing ->
                    existing.getAmount().compareTo(t.getAmount()) == 0 &&
                    existing.getRecipientAccountId().equals(t.getRecipientAccountId()) &&
                    !existing.getTransactionId().equals(t.getTransactionId())
            );

            if (duplicate) {
                String message = String.format(
                        "Possible duplicate transaction detected: same amount %s to %s within 5 minutes",
                        t.getAmount(), t.getRecipientAccountId());
                log.warn("checkDuplicateTransaction::{}", message);

                results.add(FlagResult.builder()
                        .alertType(Alert.AlertType.DUPLICATE_TRANSACTION)
                        .severity(Alert.AlertSeverity.HIGH)
                        .message(message)
                        .build());
            }
        } catch (Exception ex) {
            log.error("checkDuplicateTransaction::Error checking duplicate transaction for {} - {}",
                    t.getTransactionId(), ex.getMessage(), ex);
        }
    }

    // ── FlagResult DTO

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FlagResult {
        private Alert.AlertType alertType;
        private Alert.AlertSeverity severity;
        private String message;
    }
}
