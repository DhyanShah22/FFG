package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssignmentType;

import java.time.Instant;
import java.util.UUID;

public record AssessmentConfigurationGroupAssignmentResponse(
        UUID id,
        AssignmentType assignmentType,
        UUID assignmentId,
        Instant effectiveFrom,
        Instant effectiveTo,
        boolean isActive,
        UUID assignedBy,
        Instant assignedAt
) {
}
