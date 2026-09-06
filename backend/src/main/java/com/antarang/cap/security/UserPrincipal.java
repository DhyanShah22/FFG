package com.antarang.cap.security;

import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.entity.UserRole;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.domain.enums.ScopeType;
import com.antarang.cap.domain.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final UUID tenantId;
    private final String email;
    private final String passwordHash;
    private final UUID primaryOrgUnitId;
    private final String primaryRole;
    private final ScopeType scopeType;
    private final UUID scopeId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserPrincipal(
            UUID id,
            UUID tenantId,
            String email,
            String passwordHash,
            UUID primaryOrgUnitId,
            String primaryRole,
            ScopeType scopeType,
            UUID scopeId,
            Collection<? extends GrantedAuthority> authorities,
            boolean enabled
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.primaryOrgUnitId = primaryOrgUnitId;
        this.primaryRole = primaryRole;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static UserPrincipal from(User user) {
        Set<UserRole> activeRoles = user.getUserRoles().stream()
                .filter(userRole -> userRole.isActive() && !userRole.isDeleted())
                .collect(Collectors.toSet());

        String primaryRole = user.getProfileType().name();
        ScopeType scopeType = ScopeType.GLOBAL;
        UUID scopeId = null;

        for (UserRole userRole : activeRoles) {
            if (userRole.getRole().getName().name().equals(primaryRole)) {
                scopeType = userRole.getScopeType();
                scopeId = userRole.getScopeId();
                break;
            }
        }

        var authorities = activeRoles.stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName().name()))
                .collect(Collectors.toSet());

        boolean enabled = user.isActive()
                && !user.isDeleted()
                && user.getStatus() == UserStatus.ACTIVE;

        return new UserPrincipal(
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getPrimaryOrgUnit() != null ? user.getPrimaryOrgUnit().getId() : null,
                primaryRole,
                scopeType,
                scopeId,
                authorities,
                enabled
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    public UUID getPrimaryOrgUnitId() {
        return primaryOrgUnitId;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public UUID getScopeId() {
        return scopeId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasRole(RoleName roleName) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleName.name()));
    }
}
