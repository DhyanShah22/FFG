package com.antarang.cap.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RecommendationRunSummaryResponse(
        UUID recommendationRunId,
        UUID studentId,
        UUID configurationGroupId,
        UUID iarLogicVersionId,
        String status,
        Instant generatedAt
) {
}
