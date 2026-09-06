package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.CompletionRule;
import com.antarang.cap.domain.enums.TimerMode;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AssessmentConfigurationResponse(
        UUID id,
        UUID assessmentVersionId,
        TimerMode timerMode,
        Integer maxDurationMinutes,
        boolean autoSubmitEnabled,
        boolean showTimerToStudent,
        boolean allowResume,
        boolean restartOnInterruption,
        boolean allowReattempt,
        Integer reattemptAfterDays,
        int maxAttempts,
        CompletionRule completionRule,
        Map<String, Object> configJson,
        Instant createdAt
) {
}
