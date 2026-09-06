package com.antarang.cap.dto.response;

import java.time.Instant;
import java.util.UUID;

public record IntegrationLatestReportResponse(
        UUID reportId,
        UUID studentId,
        String fileName,
        String downloadUrl,
        Instant generatedAt
) {
}
