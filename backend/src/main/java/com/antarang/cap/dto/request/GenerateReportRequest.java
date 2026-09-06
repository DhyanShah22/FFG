package com.antarang.cap.dto.request;

import java.util.UUID;

public record GenerateReportRequest(
        UUID attemptId,
        UUID languageId,
        UUID recommendationRunId
) {
}
