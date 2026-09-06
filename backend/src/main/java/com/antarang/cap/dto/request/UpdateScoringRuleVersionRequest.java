package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UpdateScoringRuleVersionRequest(
        @NotNull Map<String, Object> ruleDefinition,
        String versionNotes
) {
}
