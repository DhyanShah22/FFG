package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateScoringRuleVersionRequest(
        Integer versionNumber,
        @NotNull Map<String, Object> ruleDefinition,
        String versionNotes
) {
}
