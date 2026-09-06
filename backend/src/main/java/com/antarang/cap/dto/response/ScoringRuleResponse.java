package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.VersionStatus;

import java.time.Instant;
import java.util.UUID;

public record ScoringRuleResponse(
        UUID id,
        UUID tenantId,
        UUID assessmentId,
        String code,
        String name,
        String description,
        LifecycleStatus status,
        UUID currentVersionId,
        VersionStatus latestVersionStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
