package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.QuestionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AttemptQuestionResponse(
        UUID questionId,
        QuestionType questionType,
        String questionText,
        boolean isMandatory,
        int displayOrder,
        String sectionCode,
        List<AttemptQuestionOptionResponse> options,
        Map<String, Object> conditionConfig,
        List<UUID> selectedOptionIds,
        String responseText,
        BigDecimal responseNumeric,
        boolean isSkipped
) {
}
