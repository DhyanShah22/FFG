package com.antarang.cap.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public class IntegrationClientPrincipal implements Authentication {

    private final UUID clientId;
    private final UUID tenantId;
    private final String clientCode;
    private boolean authenticated = true;

    public IntegrationClientPrincipal(UUID clientId, UUID tenantId, String clientCode) {
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.clientCode = clientCode;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getClientCode() {
        return clientCode;
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_INTEGRATION_CLIENT"));
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return clientCode;
    }
}
