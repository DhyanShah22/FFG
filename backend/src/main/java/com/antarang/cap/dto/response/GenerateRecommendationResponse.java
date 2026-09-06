package com.antarang.cap.dto.response;

import java.util.List;
import java.util.UUID;

public record GenerateRecommendationResponse(
        UUID recommendationRunId,
        UUID studentId,
        List<RecommendationItemResponse> recommendations
) {
}
