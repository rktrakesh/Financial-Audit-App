package com.auditSystem;

import com.auditSystem.dto.TransactionDTO;
import com.auditSystem.model.Transaction;
import com.auditSystem.repository.TransactionRepository;
import com.auditSystem.rules.FraudDetectionRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FraudDetectionTest {

    @Autowired
    private FraudDetectionRules fraudDetectionRules;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanup() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldFlagLargeTransaction() {
        Transaction t = buildTransaction("ACC001", "ACC002", new BigDecimal("15000"));
        List<FraudDetectionRules.FlagResult> flags = fraudDetectionRules.evaluate(t);
        assertTrue(flags.stream().anyMatch(f ->
                f.getAlertType().name().equals("LARGE_TRANSACTION")));
    }

    @Test
    void shouldNotFlagNormalTransaction() {
        Transaction t = buildTransaction("ACC003", "ACC004", new BigDecimal("500"));
        List<FraudDetectionRules.FlagResult> flags = fraudDetectionRules.evaluate(t);
        assertTrue(flags.isEmpty());
    }

    @Test
    void shouldFlagRapidTransactions() {
        // Insert 5 recent transactions for same account
        for (int i = 0; i < 5; i++) {
            Transaction t = buildTransaction("RAPID_ACC", "OTHER_ACC", new BigDecimal("100"));
            t.setTransactionDate(LocalDateTime.now().minusMinutes(i));
            transactionRepository.save(t);
        }
        Transaction newTx = buildTransaction("RAPID_ACC", "OTHER_ACC", new BigDecimal("100"));
        List<FraudDetectionRules.FlagResult> flags = fraudDetectionRules.evaluate(newTx);
        assertTrue(flags.stream().anyMatch(f ->
                f.getAlertType().name().equals("RAPID_TRANSACTIONS")));
    }

    private Transaction buildTransaction(String from, String to, BigDecimal amount) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .accountId(from)
                .recipientAccountId(to)
                .amount(amount)
                .currency("USD")
                .transactionType(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.PENDING)
                .transactionDate(LocalDateTime.now())
                .build();
    }
}
