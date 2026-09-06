package com.antarang.cap.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record RecommendationItemResponse(
        UUID careerId,
        String careerName,
        int rankOrder,
        BigDecimal recommendationScore,
        String reason,
        Map<String, Object> matchBreakdown
) {
}
