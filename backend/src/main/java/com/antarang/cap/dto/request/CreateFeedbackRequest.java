package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.FeedbackVisibility;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateFeedbackRequest(
        UUID reportId,
        @NotBlank String feedbackText,
        String recommendationNotes,
        FeedbackVisibility visibility
) {
}
