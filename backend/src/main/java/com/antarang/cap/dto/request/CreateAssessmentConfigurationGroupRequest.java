package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAssessmentConfigurationGroupRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        String academicYear,
        String assessmentCycle
) {
}
