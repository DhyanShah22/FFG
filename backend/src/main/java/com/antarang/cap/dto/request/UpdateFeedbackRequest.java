package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.FeedbackVisibility;

import java.util.UUID;

public record UpdateFeedbackRequest(
        String feedbackText,
        String recommendationNotes,
        FeedbackVisibility visibility,
        UUID reportId
) {
}
