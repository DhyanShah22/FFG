package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateConfigurationRequest(
        @NotNull UUID configurationGroupId,
        @NotBlank String code,
        @NotBlank String value,
        int displayOrder,
        Map<String, Object> metadata
) {
}
