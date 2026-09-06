package com.antarang.cap.domain.entity;

import com.antarang.cap.common.BaseEntity;
import com.antarang.cap.domain.enums.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private RoleName code;

    @Column(name = "name", nullable = false, length = 100)
    private String displayName;

    private String description;

    @Column(name = "is_system_role", nullable = false)
    private boolean systemRole = false;

    @ManyToMany
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    /** Role code aligned with {@link RoleName} (IF2AF0203). */
    public RoleName getCode() {
        return code;
    }

    public void setCode(RoleName code) {
        this.code = code;
    }

    /** Backward-compatible alias for authorization checks. */
    public RoleName getName() {
        return code;
    }

    public void setName(RoleName code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
