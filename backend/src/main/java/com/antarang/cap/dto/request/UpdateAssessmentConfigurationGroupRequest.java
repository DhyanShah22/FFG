package com.antarang.cap.dto.request;

public record UpdateAssessmentConfigurationGroupRequest(
        String name,
        String description,
        String academicYear,
        String assessmentCycle
) {
}
