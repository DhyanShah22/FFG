package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateScoringRuleRequest(
        @NotNull UUID assessmentId,
        @NotBlank String code,
        @NotBlank String name,
        String description
) {
}
