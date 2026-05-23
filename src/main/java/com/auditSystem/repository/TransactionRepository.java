package com.auditSystem.repository;

import com.auditSystem.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    // Paginated queries
    Page<Transaction> findByAccountId(String accountId, Pageable pageable);
    Page<Transaction> findByStatus(Transaction.TransactionStatus status, Pageable pageable);

    // Non-paginated (used for fraud detection)
    List<Transaction> findByAccountId(String accountId);

    // Count queries for dashboard
    long countByStatus(Transaction.TransactionStatus status);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") Transaction.TransactionStatus status);

    // Fraud detection queries
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId " +
           "AND t.transactionDate >= :since ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentByAccount(@Param("accountId") String accountId,
                                          @Param("since") LocalDateTime since);

    long countByAccountIdAndTransactionDateAfter(String accountId, LocalDateTime after);

    @Query("SELECT t FROM Transaction t WHERE t.amount >= :threshold")
    List<Transaction> findLargeTransactions(@Param("threshold") BigDecimal threshold);

    boolean existsByTransactionId(String transactionId);
}
