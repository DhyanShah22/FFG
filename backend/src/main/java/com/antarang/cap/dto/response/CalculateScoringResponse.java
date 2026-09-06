package com.antarang.cap.dto.response;

import java.util.List;
import java.util.UUID;

public record CalculateScoringResponse(
        UUID attemptId,
        List<DomainScoreResponse> scores
) {
}
