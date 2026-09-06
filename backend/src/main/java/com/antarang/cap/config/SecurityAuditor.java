package com.antarang.cap.config;

import com.antarang.cap.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityAuditor {

    private SecurityAuditor() {
    }

    public static UUID getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }
}
