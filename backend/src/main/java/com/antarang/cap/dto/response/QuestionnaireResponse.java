package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.VersionStatus;

import java.util.List;
import java.util.UUID;

public record QuestionnaireResponse(
        UUID id,
        UUID tenantId,
        AssessmentType assessmentType,
        String code,
        String name,
        String description,
        LifecycleStatus status,
        UUID currentVersionId,
        boolean shuffleQuestions,
        boolean isActive,
        UUID versionId,
        Integer versionNumber,
        VersionStatus versionStatus,
        List<QuestionnaireQuestionSummaryResponse> questions
) {
}
