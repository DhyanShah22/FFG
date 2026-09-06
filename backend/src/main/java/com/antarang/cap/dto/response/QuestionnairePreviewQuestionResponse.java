package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuestionnairePreviewQuestionResponse(
        UUID questionId,
        String questionCode,
        QuestionType questionType,
        String questionText,
        String helpText,
        int displayOrder,
        boolean isMandatory,
        String sectionCode,
        String sectionName,
        Map<String, Object> conditionConfig,
        List<QuestionnairePreviewOptionResponse> options
) {
}
