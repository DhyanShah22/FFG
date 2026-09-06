package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record QuestionnaireQuestionItemRequest(
        @NotNull UUID questionId,
        @NotNull Integer displayOrder,
        Boolean isMandatory,
        String sectionCode,
        String sectionName,
        Map<String, Object> conditionConfig
) {
}
