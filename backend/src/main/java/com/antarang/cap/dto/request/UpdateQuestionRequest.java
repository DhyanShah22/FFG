package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.DifficultyLevel;
import com.antarang.cap.domain.enums.ReviewStatus;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Question update. questionCode is immutable. Fields are optional; only non-null values are applied.
 * When {@code options} is provided, missing existing options are treated as removals.
 */
public record UpdateQuestionRequest(
        UUID categoryId,
        String questionType,
        String defaultText,
        String helpText,
        DifficultyLevel difficultyLevel,
        ReviewStatus reviewStatus,
        Boolean isRequiredDefault,
        Boolean scoringEnabled,
        Boolean isActive,
        Map<String, Object> metadata,
        @Valid List<UpdateQuestionOptionRequest> options
) {
}
