package com.auditSystem.controller;

import com.auditSystem.dto.TransactionDTO;
import com.auditSystem.service.TransactionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction management and monitoring endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    // POST /api/transactions — Submit a new transaction
    @PostMapping
    @Operation(
            summary = "Submit New Transaction",
            description = "Submit a new financial transaction for processing and fraud detection analysis."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
            @ApiResponse(responseCode = "422", description = "Transaction failed fraud validation")
    })
    public ResponseEntity<TransactionDTO.Response> submit(
            @Valid @RequestBody TransactionDTO.Request request) {
        log.info("submit::Submitting new transaction for account: {}", request.getAccountId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.submitTransaction(request));
    }

    // GET /api/transactions?page=0&size=20&sort=transactionDate,desc
    @GetMapping
    @Operation(
            summary = "Get All Transactions (Paginated)",
            description = "Retrieve all transactions with pagination support. Default sorting by transaction date (descending)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<Page<TransactionDTO.Response>> getAll(
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        log.info("getAll::Retrieving transactions page {}", pageable.getPageNumber());
        return ResponseEntity.ok(transactionService.getAllTransactions(pageable));
    }

    // GET /api/transactions/{transactionId}
    @GetMapping("/{transactionId}")
    @Operation(
            summary = "Get Transaction by ID",
            description = "Retrieve detailed information about a specific transaction."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionDTO.Response> getOne(
            @PathVariable
            @Parameter(description = "Unique transaction identifier")
            String transactionId) {
        log.info("getOne::Retrieving transaction: {}", transactionId);
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    // GET /api/transactions/account/{accountId}?page=0&size=20
    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "Get Transactions by Account",
            description = "Retrieve all transactions for a specific account with pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account transactions retrieved"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Page<TransactionDTO.Response>> getByAccount(
            @PathVariable
            @Parameter(description = "Account identifier")
            String accountId,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        log.info("getByAccount::Retrieving transactions for account: {}", accountId);
        return ResponseEntity.ok(transactionService.getByAccount(accountId, pageable));
    }

    // GET /api/transactions/flagged?page=0&size=20
    @GetMapping("/flagged")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get Flagged Transactions",
            description = "Retrieve transactions flagged for fraud detection. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flagged transactions retrieved"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    public ResponseEntity<Page<TransactionDTO.Response>> getFlagged(
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        log.info("getFlagged::Retrieving all flagged transactions");
        return ResponseEntity.ok(transactionService.getFlaggedTransactions(pageable));
    }

    // PUT /api/transactions/{transactionId}/approve
    @PutMapping("/{transactionId}/approve")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Approve Transaction",
            description = "Approve a flagged transaction after manual review. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction approved successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TransactionDTO.Response> approve(
            @PathVariable
            @Parameter(description = "Transaction ID to approve")
            String transactionId,
            @RequestParam(defaultValue = "AUDITOR")
            @Parameter(description = "Name of the person approving the transaction")
            String approvedBy) {
        log.info("approve::Approving transaction {} by {}", transactionId, approvedBy);
        return ResponseEntity.ok(transactionService.approveTransaction(transactionId, approvedBy));
    }

    // GET /api/transactions/dashboard
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get Dashboard Summary",
            description = "Retrieve transaction dashboard statistics and summary metrics. Requires AUDITOR or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard summary retrieved"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TransactionDTO.DashboardSummary> dashboard() {
        log.info("dashboard::Retrieving dashboard summary");
        return ResponseEntity.ok(transactionService.getDashboardSummary());
    }
}
