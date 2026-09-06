package com.antarang.cap.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AssessmentConfigurationGroupOutputResponse(
        UUID id,
        UUID reportTemplateVersionId,
        UUID outputLanguageId,
        Map<String, Object> outputConfig,
        boolean isActive,
        Instant createdAt
) {
}
