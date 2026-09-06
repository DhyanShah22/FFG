package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.FeedbackVisibility;

import java.time.Instant;
import java.util.UUID;

public record FeedbackResponse(
        UUID id,
        UUID studentId,
        UUID counsellorId,
        UUID reportId,
        String feedbackText,
        String recommendationNotes,
        FeedbackVisibility visibility,
        Instant createdAt,
        Instant updatedAt
) {
}
