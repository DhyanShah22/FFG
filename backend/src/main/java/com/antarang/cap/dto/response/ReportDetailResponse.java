package com.antarang.cap.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReportDetailResponse(
        UUID reportId,
        UUID studentId,
        UUID templateVersionId,
        UUID recommendationRunId,
        UUID configurationGroupId,
        String reportType,
        UUID languageId,
        String fileName,
        String downloadUrl,
        String s3Key,
        Instant generatedAt,
        UUID generatedBy
) {
}
