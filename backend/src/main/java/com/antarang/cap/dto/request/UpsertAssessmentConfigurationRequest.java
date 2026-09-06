package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.CompletionRule;
import com.antarang.cap.domain.enums.TimerMode;

import java.util.Map;

public record UpsertAssessmentConfigurationRequest(
        TimerMode timerMode,
        Integer maxDurationMinutes,
        Boolean autoSubmitEnabled,
        Boolean showTimerToStudent,
        Boolean allowResume,
        Boolean restartOnInterruption,
        Boolean allowReattempt,
        Integer reattemptAfterDays,
        Integer maxAttempts,
        CompletionRule completionRule,
        Map<String, Object> configJson
) {
}
