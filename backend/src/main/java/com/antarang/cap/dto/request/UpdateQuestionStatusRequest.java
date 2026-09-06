package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ReviewStatus;

/**
 * Partial status update. All fields optional; only non-null values are applied.
 */
public record UpdateQuestionStatusRequest(
        ReviewStatus reviewStatus,
        Boolean isActive
) {
}
