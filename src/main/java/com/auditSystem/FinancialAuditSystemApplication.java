package com.auditSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinancialAuditSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinancialAuditSystemApplication.class, args);
    }
}
