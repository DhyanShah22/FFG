package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.LifecycleStatus;

import java.util.UUID;

public record QuestionnaireListItemResponse(
        UUID id,
        AssessmentType assessmentType,
        String code,
        String name,
        String description,
        LifecycleStatus status,
        UUID currentVersionId,
        boolean shuffleQuestions,
        boolean isActive
) {
}
