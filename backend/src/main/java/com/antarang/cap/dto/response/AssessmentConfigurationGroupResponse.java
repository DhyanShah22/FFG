package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.LifecycleStatus;

import java.time.Instant;
import java.util.UUID;

public record AssessmentConfigurationGroupResponse(
        UUID id,
        UUID tenantId,
        String code,
        String name,
        String description,
        String academicYear,
        String assessmentCycle,
        LifecycleStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
