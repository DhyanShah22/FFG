package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.VersionStatus;

import java.time.Instant;
import java.util.UUID;

public record QuestionnaireVersionSummaryResponse(
        UUID id,
        int versionNumber,
        VersionStatus status,
        Instant publishedAt,
        long questionCount
) {
}
