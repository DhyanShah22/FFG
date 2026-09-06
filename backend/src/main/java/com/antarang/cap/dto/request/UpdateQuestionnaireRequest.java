package com.antarang.cap.dto.request;

import jakarta.validation.Valid;

import java.util.List;

public record UpdateQuestionnaireRequest(
        String name,
        String description,
        Boolean shuffleQuestions,
        @Valid List<QuestionnaireQuestionItemRequest> questions
) {
}
