package com.auditSystem.controller;

import com.auditSystem.dto.TransactionDTO;
import com.auditSystem.service.TransactionService;
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
public class TransactionController {

    private final TransactionService transactionService;

    // POST /api/transactions — Submit a new transaction
    @PostMapping
    public ResponseEntity<TransactionDTO.Response> submit(
            @Valid @RequestBody TransactionDTO.Request request) {
        log.info("submit::Submitting new transaction for account: {}", request.getAccountId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.submitTransaction(request));
    }

    // GET /api/transactions?page=0&size=20&sort=transactionDate,desc
    @GetMapping
    public ResponseEntity<Page<TransactionDTO.Response>> getAll(
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("getAll::Retrieving transactions page {}", pageable.getPageNumber());
        return ResponseEntity.ok(transactionService.getAllTransactions(pageable));
    }

    // GET /api/transactions/{transactionId}
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO.Response> getOne(
            @PathVariable String transactionId) {
        log.info("getOne::Retrieving transaction: {}", transactionId);
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    // GET /api/transactions/account/{accountId}?page=0&size=20
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<TransactionDTO.Response>> getByAccount(
            @PathVariable String accountId,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("getByAccount::Retrieving transactions for account: {}", accountId);
        return ResponseEntity.ok(transactionService.getByAccount(accountId, pageable));
    }

    // GET /api/transactions/flagged?page=0&size=20
    @GetMapping("/flagged")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Page<TransactionDTO.Response>> getFlagged(
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("getFlagged::Retrieving all flagged transactions");
        return ResponseEntity.ok(transactionService.getFlaggedTransactions(pageable));
    }

    // PUT /api/transactions/{transactionId}/approve
    @PutMapping("/{transactionId}/approve")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<TransactionDTO.Response> approve(
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "AUDITOR") String approvedBy) {
        log.info("approve::Approving transaction {} by {}", transactionId, approvedBy);
        return ResponseEntity.ok(transactionService.approveTransaction(transactionId, approvedBy));
    }

    // GET /api/transactions/dashboard
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<TransactionDTO.DashboardSummary> dashboard() {
        log.info("dashboard::Retrieving dashboard summary");
        return ResponseEntity.ok(transactionService.getDashboardSummary());
    }
}
