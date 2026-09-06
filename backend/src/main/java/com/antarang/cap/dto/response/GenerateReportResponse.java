package com.antarang.cap.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GenerateReportResponse(
        UUID reportId,
        UUID studentId,
        String fileName,
        String downloadUrl,
        UUID languageId,
        Instant generatedAt
) {
}
