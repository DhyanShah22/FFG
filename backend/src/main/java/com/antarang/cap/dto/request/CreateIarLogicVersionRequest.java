package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateIarLogicVersionRequest(
        Integer versionNumber,
        @NotNull Map<String, Object> logicDefinition,
        String versionNotes
) {
}
