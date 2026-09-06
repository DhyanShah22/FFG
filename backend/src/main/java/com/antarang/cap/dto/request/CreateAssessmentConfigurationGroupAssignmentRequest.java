package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.AssignmentType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateAssessmentConfigurationGroupAssignmentRequest(
        @NotNull AssignmentType assignmentType,
        @NotNull UUID assignmentId,
        Instant effectiveFrom,
        Instant effectiveTo
) {
}
