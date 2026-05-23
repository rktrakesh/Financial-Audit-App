package com.auditSystem.controller;

import com.auditSystem.model.AuditLog;
import com.auditSystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditLogService auditLogService;

    // GET /api/audit?page=0&size=20
    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAll(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("getAll::Retrieving audit logs paged");
        return ResponseEntity.ok(auditLogService.getAllPaged(pageable));
    }

    // GET /api/audit/transaction/{transactionId}
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<AuditLog>> getByTransaction(@PathVariable String transactionId) {
        log.info("getByTransaction::Retrieving audit logs for transaction: {}", transactionId);
        return ResponseEntity.ok(auditLogService.getByTransaction(transactionId));
    }

    // GET /api/audit/account/{accountId}
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<AuditLog>> getByAccount(@PathVariable String accountId) {
        log.info("getByAccount::Retrieving audit logs for account: {}", accountId);
        return ResponseEntity.ok(auditLogService.getByAccount(accountId));
    }

    // GET /api/audit/severity/{severity}
    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<AuditLog>> getBySeverity(@PathVariable AuditLog.AuditSeverity severity) {
        log.info("getBySeverity::Retrieving audit logs with severity: {}", severity);
        return ResponseEntity.ok(auditLogService.getBySeverity(severity));
    }

    // GET /api/audit/range?start=...&end=...
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<List<AuditLog>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("getByDateRange::Retrieving audit logs between {} and {}", start, end);
        return ResponseEntity.ok(auditLogService.getByDateRange(start, end));
    }
}
