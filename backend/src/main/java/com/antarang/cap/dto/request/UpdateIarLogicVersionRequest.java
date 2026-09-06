package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UpdateIarLogicVersionRequest(
        @NotNull Map<String, Object> logicDefinition
) {
}
