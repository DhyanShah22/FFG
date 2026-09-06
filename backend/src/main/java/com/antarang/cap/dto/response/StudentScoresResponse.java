package com.antarang.cap.dto.response;

import java.util.List;
import java.util.UUID;

public record StudentScoresResponse(
        UUID studentId,
        UUID attemptId,
        List<DomainScoreResponse> scores
) {
}
