package com.auditSystem.service;

import com.auditSystem.dto.TransactionDTO;
import com.auditSystem.exception.TransactionNotFoundException;
import com.auditSystem.model.Alert;
import com.auditSystem.model.AuditLog;
import com.auditSystem.model.Transaction;
import com.auditSystem.repository.TransactionRepository;
import com.auditSystem.rules.FraudDetectionRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudDetectionRules fraudDetectionRules;
    private final AuditLogService auditLogService;
    private final AlertService alertService;

    // ── Submit Transaction ────────────────────────────────────────────────────

    @Transactional
    public TransactionDTO.Response submitTransaction(TransactionDTO.Request request) {
        log.info("submitTransaction::Processing transaction for account: {}", request.getAccountId());

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .accountId(request.getAccountId())
                .recipientAccountId(request.getRecipientAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .transactionType(request.getTransactionType())
                .status(Transaction.TransactionStatus.PENDING)
                .transactionDate(LocalDateTime.now())
                .description(request.getDescription())
                .ipAddress(request.getIpAddress())
                .location(request.getLocation())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.debug("submitTransaction::Transaction saved with ID: {}", saved.getTransactionId());

        auditLogService.log(
                saved.getTransactionId(), saved.getAccountId(),
                AuditLog.AuditAction.TRANSACTION_CREATED,
                String.format("Transaction created: %s %s from %s to %s",
                        saved.getAmount(), saved.getCurrency(),
                        saved.getAccountId(), saved.getRecipientAccountId()),
                AuditLog.AuditSeverity.LOW, "SYSTEM", request.getIpAddress()
        );

        List<FraudDetectionRules.FlagResult> flags = fraudDetectionRules.evaluate(saved);
        List<String> flagReasons = flags.stream()
                .map(FraudDetectionRules.FlagResult::getMessage)
                .collect(Collectors.toList());

        if (!flags.isEmpty()) {
            log.warn("submitTransaction::Fraud flags detected for transaction {}: {} flags",
                    saved.getTransactionId(), flags.size());

            boolean isCritical = flags.stream()
                    .anyMatch(f -> f.getSeverity() == Alert.AlertSeverity.CRITICAL);

            if (isCritical) {
                saved.setStatus(Transaction.TransactionStatus.BLOCKED);
                log.error("submitTransaction::Transaction {} BLOCKED due to critical fraud indicators",
                        saved.getTransactionId());
                auditLogService.log(saved.getTransactionId(), saved.getAccountId(),
                        AuditLog.AuditAction.TRANSACTION_BLOCKED,
                        "Transaction blocked due to critical fraud indicators: " + flagReasons,
                        AuditLog.AuditSeverity.CRITICAL, "SYSTEM", request.getIpAddress());
            } else {
                saved.setStatus(Transaction.TransactionStatus.FLAGGED);
                log.warn("submitTransaction::Transaction {} FLAGGED for review", saved.getTransactionId());
                auditLogService.log(saved.getTransactionId(), saved.getAccountId(),
                        AuditLog.AuditAction.TRANSACTION_FLAGGED,
                        "Transaction flagged for review: " + flagReasons,
                        AuditLog.AuditSeverity.HIGH, "SYSTEM", request.getIpAddress());
            }

            flags.forEach(flag -> {
                try {
                    alertService.createAlert(saved.getTransactionId(), saved.getAccountId(),
                            flag.getAlertType(), flag.getSeverity(), flag.getMessage());
                    auditLogService.log(saved.getTransactionId(), saved.getAccountId(),
                            AuditLog.AuditAction.ALERT_GENERATED,
                            "Alert generated: " + flag.getMessage(),
                            AuditLog.AuditSeverity.HIGH, "SYSTEM", request.getIpAddress());
                } catch (Exception ex) {
                    log.error("submitTransaction::Error creating alert for transaction {} - {}",
                            saved.getTransactionId(), ex.getMessage(), ex);
                }
            });

            transactionRepository.save(saved);
        } else {
            saved.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(saved);
            log.info("submitTransaction::Transaction {} completed cleanly", saved.getTransactionId());
        }

        return buildResponse(saved, flagReasons);
    }

    // ── Approve Flagged Transaction ───────────────────────────────────────────

    @Transactional
    public TransactionDTO.Response approveTransaction(String transactionId, String approvedBy) {
        log.info("approveTransaction::Approving transaction {} by {}", transactionId, approvedBy);
        Transaction t = findByTransactionId(transactionId);

        if (t.getStatus() != Transaction.TransactionStatus.FLAGGED) {
            throw new IllegalStateException("Only FLAGGED transactions can be approved. Current status: " + t.getStatus());
        }

        t.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactionRepository.save(t);
        log.info("approveTransaction::Transaction {} successfully approved", transactionId);

        auditLogService.log(t.getTransactionId(), t.getAccountId(),
                AuditLog.AuditAction.TRANSACTION_APPROVED,
                "Transaction manually approved by: " + approvedBy,
                AuditLog.AuditSeverity.MEDIUM, approvedBy, null);

        return buildResponse(t, List.of());
    }

    // ── Paginated Queries ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TransactionDTO.Response> getAllTransactions(Pageable pageable) {
        log.info("getAllTransactions::Retrieving transactions page {}", pageable.getPageNumber());
        return transactionRepository.findAll(pageable).map(t -> buildResponse(t, List.of()));
    }

    @Transactional(readOnly = true)
    public Page<TransactionDTO.Response> getByAccount(String accountId, Pageable pageable) {
        log.info("getByAccount::Retrieving transactions for account {}", accountId);
        return transactionRepository.findByAccountId(accountId, pageable)
                .map(t -> buildResponse(t, List.of()));
    }

    @Transactional(readOnly = true)
    public Page<TransactionDTO.Response> getFlaggedTransactions(Pageable pageable) {
        log.info("getFlaggedTransactions::Retrieving flagged transactions");
        return transactionRepository.findByStatus(Transaction.TransactionStatus.FLAGGED, pageable)
                .map(t -> buildResponse(t, List.of()));
    }

    @Transactional(readOnly = true)
    public TransactionDTO.Response getTransaction(String transactionId) {
        log.info("getTransaction::Retrieving transaction {}", transactionId);
        return buildResponse(findByTransactionId(transactionId), List.of());
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TransactionDTO.DashboardSummary getDashboardSummary() {
        log.info("getDashboardSummary::Generating dashboard summary");

        long total = transactionRepository.count();
        long flagged = transactionRepository.countByStatus(Transaction.TransactionStatus.FLAGGED);
        long blocked = transactionRepository.countByStatus(Transaction.TransactionStatus.BLOCKED);
        long completed = transactionRepository.countByStatus(Transaction.TransactionStatus.COMPLETED);
        BigDecimal totalAmount = transactionRepository.sumAmountByStatus(Transaction.TransactionStatus.COMPLETED);

        return TransactionDTO.DashboardSummary.builder()
                .totalTransactions(total)
                .flaggedTransactions(flagged)
                .blockedTransactions(blocked)
                .completedTransactions(completed)
                .openAlerts(alertService.countOpenAlerts())
                .criticalAlerts(alertService.countCriticalAlerts())
                .totalAmountProcessed(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Transaction findByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    private TransactionDTO.Response buildResponse(Transaction t, List<String> flagReasons) {
        return TransactionDTO.Response.builder()
                .id(t.getId())
                .transactionId(t.getTransactionId())
                .accountId(t.getAccountId())
                .recipientAccountId(t.getRecipientAccountId())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .transactionType(t.getTransactionType())
                .status(t.getStatus())
                .transactionDate(t.getTransactionDate())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .flagReasons(flagReasons)
                .build();
    }
}
