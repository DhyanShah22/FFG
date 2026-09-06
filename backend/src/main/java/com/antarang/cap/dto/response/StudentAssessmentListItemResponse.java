package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.TimerMode;

import java.util.UUID;

public record StudentAssessmentListItemResponse(
        UUID assessmentId,
        String assessmentCode,
        String assessmentName,
        UUID configurationGroupId,
        String status,
        TimerMode timerMode,
        boolean allowResume,
        int maxAttempts,
        long attemptsUsed,
        UUID activeAttemptId
) {
}
