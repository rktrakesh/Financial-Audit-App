package com.auditSystem.controller;

import com.auditSystem.model.AuditLog;
import com.auditSystem.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Audit Logs", description = "Audit trail and compliance logging endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {

    private final AuditLogService auditLogService;

    // GET /api/audit?page=0&size=20
    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get All Audit Logs (Paginated)",
            description = "Retrieve complete audit trail with pagination. Default sorting by timestamp (descending). Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<AuditLog>> getAll(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        log.info("getAll::Retrieving audit logs paged");
        return ResponseEntity.ok(auditLogService.getAllPaged(pageable));
    }

    // GET /api/audit/transaction/{transactionId}
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get Audit Logs by Transaction",
            description = "Retrieve complete audit trail for a specific transaction. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction audit logs retrieved"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<AuditLog>> getByTransaction(
            @PathVariable
            @Parameter(description = "Transaction identifier")
            String transactionId) {
        log.info("getByTransaction::Retrieving audit logs for transaction: {}", transactionId);
        return ResponseEntity.ok(auditLogService.getByTransaction(transactionId));
    }

    // GET /api/audit/account/{accountId}
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get Audit Logs by Account",
            description = "Retrieve all audit activities for a specific account. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account audit logs retrieved"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<AuditLog>> getByAccount(
            @PathVariable
            @Parameter(description = "Account identifier")
            String accountId) {
        log.info("getByAccount::Retrieving audit logs for account: {}", accountId);
        return ResponseEntity.ok(auditLogService.getByAccount(accountId));
    }

    // GET /api/audit/severity/{severity}
    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get Audit Logs by Severity",
            description = "Retrieve audit logs filtered by severity level (INFO, WARNING, ERROR, CRITICAL). Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered audit logs retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid severity level"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<AuditLog>> getBySeverity(
            @PathVariable
            @Parameter(description = "Audit log severity level")
            AuditLog.AuditSeverity severity) {
        log.info("getBySeverity::Retrieving audit logs with severity: {}", severity);
        return ResponseEntity.ok(auditLogService.getBySeverity(severity));
    }

    // GET /api/audit/range?start=...&end=...
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get Audit Logs by Date Range",
            description = "Retrieve audit logs within a specified date/time range. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs within range retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or range"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<AuditLog>> getByDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Start date/time (ISO 8601 format: 2024-01-15T10:30:00)")
            LocalDateTime start,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "End date/time (ISO 8601 format: 2024-01-16T23:59:59)")
            LocalDateTime end) {
        log.info("getByDateRange::Retrieving audit logs between {} and {}", start, end);
        return ResponseEntity.ok(auditLogService.getByDateRange(start, end));
    }
}
