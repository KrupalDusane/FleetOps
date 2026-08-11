package com.fleetops.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
        // Prevent instantiation
    }

    /**
     * Retrieves the username of the currently authenticated user.
     * Returns "system" if no user is authenticated or the user is anonymous.
     *
     * @return the current username or "system"
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "system";
    }
}
