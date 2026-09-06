package com.antarang.cap.security;

import com.antarang.cap.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authentication required", "UNAUTHORIZED");
        }
        return principal;
    }

    public static UUID requireTenantId() {
        UUID tenantId = requirePrincipal().getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is required", "ACCESS_DENIED");
        }
        return tenantId;
    }

    public static void assertSameTenant(UUID entityTenantId) {
        if (entityTenantId == null || !entityTenantId.equals(requireTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }
    }
}
