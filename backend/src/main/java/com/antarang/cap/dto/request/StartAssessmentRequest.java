package com.antarang.cap.dto.request;

import java.util.UUID;

public record StartAssessmentRequest(
        UUID studentId,
        UUID assessmentLanguageId,
        Boolean isAdminTestAttempt,
        Boolean excludeFromAnalytics
) {
}
