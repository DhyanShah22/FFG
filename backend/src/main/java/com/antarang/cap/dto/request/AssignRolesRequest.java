package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ScopeType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AssignRolesRequest(
        @NotEmpty List<RoleAssignment> roles
) {
    public record RoleAssignment(
            @NotNull UUID roleId,
            ScopeType scopeType,
            UUID scopeId
    ) {
    }
}
