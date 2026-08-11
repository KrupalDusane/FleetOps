package com.fleetops.config;

import com.fleetops.service.AuditLogService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuditAuthenticationListener {

    private final AuditLogService auditLogService;

    public AuditAuthenticationListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            String username = extractUsername(auth.getPrincipal());
            auditLogService.logAuthAction(username, "User Logged In", "User successfully authenticated");
        }
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            String username = extractUsername(auth.getPrincipal());
            auditLogService.logAuthAction(username, "User Logged Out", "User successfully logged out");
        }
    }

    private String extractUsername(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return "Unknown";
    }
}
