package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssessmentType;

import java.util.UUID;

public record QuestionCategoryResponse(
        UUID id,
        UUID tenantId,
        AssessmentType assessmentType,
        String code,
        String name,
        String description,
        String domainCode,
        boolean isActive
) {
}
