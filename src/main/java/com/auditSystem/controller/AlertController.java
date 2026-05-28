package com.auditSystem.controller;

import com.auditSystem.dto.TransactionDTO;
import com.auditSystem.model.Alert;
import com.auditSystem.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Alerts", description = "Fraud alert management and monitoring endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AlertController {

    private final AlertService alertService;

    // GET /api/alerts?page=0&size=20
    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get All Alerts (Paginated)",
            description = "Retrieve all fraud detection alerts with pagination. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<Alert>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        log.info("getAll::Retrieving all alerts paged");
        return ResponseEntity.ok(alertService.getAllPaged(pageable));
    }

    // GET /api/alerts/open
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get Open Alerts",
            description = "Retrieve all unresolved fraud alerts. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Open alerts retrieved"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<Alert>> getOpen() {
        log.info("getOpen::Retrieving open alerts");
        return ResponseEntity.ok(alertService.getOpenAlerts());
    }

    // GET /api/alerts/account/{accountId}
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get Alerts by Account",
            description = "Retrieve all alerts for a specific account. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account alerts retrieved"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<Alert>> getByAccount(
            @PathVariable
            @Parameter(description = "Account identifier")
            String accountId) {
        log.info("getByAccount::Retrieving alerts for account: {}", accountId);
        return ResponseEntity.ok(alertService.getByAccount(accountId));
    }

    // GET /api/alerts/severity/{severity}
    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get Alerts by Severity",
            description = "Retrieve all alerts filtered by severity level (LOW, MEDIUM, HIGH, CRITICAL). Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered alerts retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid severity level"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<Alert>> getBySeverity(
            @PathVariable
            @Parameter(description = "Alert severity level")
            Alert.AlertSeverity severity) {
        log.info("getBySeverity::Retrieving alerts with severity: {}", severity);
        return ResponseEntity.ok(alertService.getBySeverity(severity));
    }

    // GET /api/alerts/transaction/{transactionId}
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Get Alerts by Transaction",
            description = "Retrieve all alerts associated with a specific transaction. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction alerts retrieved"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<Alert>> getByTransaction(
            @PathVariable
            @Parameter(description = "Transaction identifier")
            String transactionId) {
        log.info("getByTransaction::Retrieving alerts for transaction: {}", transactionId);
        return ResponseEntity.ok(alertService.getByTransaction(transactionId));
    }

    // PUT /api/alerts/{alertId}/resolve
    @PutMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(
            summary = "Resolve Alert",
            description = "Mark a fraud alert as resolved with resolution notes and action taken. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert resolved successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "400", description = "Invalid resolution request"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Alert> resolve(
            @PathVariable
            @Parameter(description = "Alert identifier to resolve")
            Long alertId,
            @Valid @RequestBody TransactionDTO.AlertResolutionRequest request) {
        log.info("resolve::Resolving alert {} by {}", alertId, request.getResolvedBy());
        return ResponseEntity.ok(alertService.resolveAlert(alertId, request));
    }
}
