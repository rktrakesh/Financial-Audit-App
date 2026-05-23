package com.auditSystem.exception;

public class AlertNotFoundException extends RuntimeException {
    public AlertNotFoundException(Long alertId) {
        super("Alert not found: " + alertId);
    }
}
