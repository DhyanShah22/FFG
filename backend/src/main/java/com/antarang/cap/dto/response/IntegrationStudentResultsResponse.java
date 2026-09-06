package com.antarang.cap.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record IntegrationStudentResultsResponse(
        UUID studentId,
        String externalStudentId,
        String attemptStatus,
        UUID attemptId,
        List<Map<String, Object>> domainScores
) {
}
