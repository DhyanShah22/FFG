package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.DifficultyLevel;
import com.antarang.cap.domain.enums.QuestionType;
import com.antarang.cap.domain.enums.ReviewStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuestionSummaryResponse(
        UUID id,
        UUID tenantId,
        AssessmentType assessmentType,
        UUID categoryId,
        String categoryCode,
        String questionCode,
        QuestionType questionType,
        String defaultText,
        String helpText,
        String localizedText,
        DifficultyLevel difficultyLevel,
        ReviewStatus reviewStatus,
        boolean isRequiredDefault,
        boolean scoringEnabled,
        boolean isActive,
        Map<String, Object> metadata
) {
}
