package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateConfigurationGroupRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        UUID tenantId,
        boolean systemDefined
) {
}
