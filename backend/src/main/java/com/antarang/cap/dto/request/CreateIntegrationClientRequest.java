package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.IntegrationClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateIntegrationClientRequest(
        @NotBlank String clientCode,
        @NotBlank String clientName,
        @NotNull IntegrationClientType clientType,
        Map<String, Object> allowedScopes
) {
}
