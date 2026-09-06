package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record AddAssessmentConfigurationGroupOutputRequest(
        UUID reportTemplateVersionId,
        @NotNull UUID outputLanguageId,
        Map<String, Object> outputConfig
) {
}
