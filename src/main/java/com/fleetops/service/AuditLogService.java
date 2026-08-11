package com.fleetops.service;

public interface AuditLogService {
    void logAction(String action, String entityName, Long entityId, Object oldValue, Object newValue, String description);
    void logAuthAction(String username, String action, String description);
}
