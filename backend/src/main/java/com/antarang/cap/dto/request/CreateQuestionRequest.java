package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.DifficultyLevel;
import com.antarang.cap.domain.enums.ReviewStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateQuestionRequest(
        @NotNull UUID categoryId,
        @NotBlank String questionCode,
        @NotBlank String questionType,
        @NotBlank String defaultText,
        String helpText,
        DifficultyLevel difficultyLevel,
        ReviewStatus reviewStatus,
        Boolean isRequiredDefault,
        Boolean scoringEnabled,
        Map<String, Object> metadata,
        @Valid List<CreateQuestionOptionRequest> options
) {
}
