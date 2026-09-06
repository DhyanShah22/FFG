package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.VersionStatus;

import java.time.Instant;
import java.util.UUID;

public record AssessmentVersionResponse(
        UUID id,
        UUID assessmentId,
        UUID questionnaireVersionId,
        int versionNumber,
        VersionStatus status,
        Instant publishedAt,
        Instant createdAt
) {
}
