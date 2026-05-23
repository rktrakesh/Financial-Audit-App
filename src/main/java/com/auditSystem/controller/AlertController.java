package com.auditSystem.controller;

import com.auditSystem.dto.TransactionDTO;
import com.auditSystem.model.Alert;
import com.auditSystem.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    // GET /api/alerts?page=0&size=20
    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Page<Alert>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("getAll::Retrieving all alerts paged");
        return ResponseEntity.ok(alertService.getAllPaged(pageable));
    }

    // GET /api/alerts/open
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<Alert>> getOpen() {
        log.info("getOpen::Retrieving open alerts");
        return ResponseEntity.ok(alertService.getOpenAlerts());
    }

    // GET /api/alerts/account/{accountId}
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<Alert>> getByAccount(@PathVariable String accountId) {
        log.info("getByAccount::Retrieving alerts for account: {}", accountId);
        return ResponseEntity.ok(alertService.getByAccount(accountId));
    }

    // GET /api/alerts/severity/{severity}
    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<Alert>> getBySeverity(@PathVariable Alert.AlertSeverity severity) {
        log.info("getBySeverity::Retrieving alerts with severity: {}", severity);
        return ResponseEntity.ok(alertService.getBySeverity(severity));
    }

    // GET /api/alerts/transaction/{transactionId}
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<Alert>> getByTransaction(@PathVariable String transactionId) {
        log.info("getByTransaction::Retrieving alerts for transaction: {}", transactionId);
        return ResponseEntity.ok(alertService.getByTransaction(transactionId));
    }

    // PUT /api/alerts/{alertId}/resolve
    @PutMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Alert> resolve(
            @PathVariable Long alertId,
            @Valid @RequestBody TransactionDTO.AlertResolutionRequest request) {
        log.info("resolve::Resolving alert {} by {}", alertId, request.getResolvedBy());
        return ResponseEntity.ok(alertService.resolveAlert(alertId, request));
    }
}
