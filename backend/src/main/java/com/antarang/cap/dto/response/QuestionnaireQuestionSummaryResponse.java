package com.antarang.cap.dto.response;

import java.util.Map;
import java.util.UUID;

public record QuestionnaireQuestionSummaryResponse(
        UUID id,
        UUID questionId,
        String questionCode,
        String defaultText,
        int displayOrder,
        boolean isMandatory,
        String sectionCode,
        String sectionName,
        Map<String, Object> conditionConfig
) {
}
