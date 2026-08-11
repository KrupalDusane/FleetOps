package com.fleetops.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fleetops.entity.AuditLog;
import com.fleetops.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public void logAction(String action, String entityName, Long entityId, Object oldValue, Object newValue, String description) {
        try {
            String currentUser = com.fleetops.util.SecurityUtils.getCurrentUsername();
            
            String oldJson = null;
            if (oldValue != null) {
                try {
                    oldJson = objectMapper.writeValueAsString(oldValue);
                } catch (Exception e) {
                    oldJson = "Error serializing: " + oldValue.toString();
                }
            }

            String newJson = null;
            if (newValue != null) {
                try {
                    newJson = objectMapper.writeValueAsString(newValue);
                } catch (Exception e) {
                    newJson = "Error serializing: " + newValue.toString();
                }
            }

            AuditLog auditLog = AuditLog.builder()
                    .username(currentUser)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            // CRITICAL: Swallow exception so business transaction is not rolled back.
            logger.error("Failed to write audit log for action: " + action + ", entity: " + entityName, ex);
        }
    }

    @Override
    public void logAuthAction(String username, String action, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username != null ? username : "system")
                    .action(action)
                    .entityName("Authentication")
                    .entityId(null)
                    .oldValue(null)
                    .newValue(null)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            logger.error("Failed to write auth audit log for user: " + username, ex);
        }
    }

}
