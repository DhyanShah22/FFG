package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.OrgUnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateOrgUnitRequest(
        @NotNull OrgUnitType orgUnitType,
        @NotBlank String code,
        @NotBlank String name,
        UUID parentOrgUnitId,
        String description,
        String address,
        Map<String, Object> metadata
) {
}
