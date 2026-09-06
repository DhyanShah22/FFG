package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateQuestionnaireRequest(
        @NotNull AssessmentType assessmentType,
        @NotBlank String code,
        @NotBlank String name,
        String description,
        Boolean shuffleQuestions
) {
}
