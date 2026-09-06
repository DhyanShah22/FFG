package com.antarang.cap.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AssessmentConfigurationGroupItemResponse(
        UUID id,
        UUID assessmentId,
        UUID assessmentVersionId,
        UUID questionnaireVersionId,
        UUID iarLogicVersionId,
        UUID scoringRuleVersionId,
        UUID assessmentLanguageId,
        boolean isRequired,
        int displayOrder,
        Instant createdAt
) {
}
