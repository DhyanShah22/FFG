package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.VersionStatus;

import java.util.List;
import java.util.UUID;

public record QuestionnairePreviewResponse(
        UUID questionnaireId,
        String questionnaireCode,
        String questionnaireName,
        AssessmentType assessmentType,
        UUID versionId,
        int versionNumber,
        VersionStatus versionStatus,
        UUID languageId,
        List<QuestionnairePreviewQuestionResponse> questions
) {
}
