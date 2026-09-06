package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAssessmentVersionRequest(
        @NotNull UUID questionnaireVersionId,
        Integer versionNumber
) {
}
