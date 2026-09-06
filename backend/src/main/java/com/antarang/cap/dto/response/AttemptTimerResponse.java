package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.TimerMode;

import java.time.Instant;

public record AttemptTimerResponse(
        TimerMode timerMode,
        Instant startedAt,
        boolean showTimerToStudent,
        boolean autoSubmitEnabled,
        Integer maxDurationMinutes,
        boolean allowResume
) {
}
