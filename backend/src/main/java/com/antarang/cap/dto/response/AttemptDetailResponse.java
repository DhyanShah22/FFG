package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.ScoreStatus;
import com.antarang.cap.domain.enums.SubmissionReason;
import com.antarang.cap.domain.enums.TimerMode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttemptDetailResponse(
        UUID attemptId,
        UUID assessmentId,
        String assessmentCode,
        String assessmentName,
        UUID configurationGroupId,
        UUID questionnaireVersionId,
        UUID assessmentLanguageId,
        AttemptStatus status,
        TimerMode timerMode,
        Instant startedAt,
        Instant submittedAt,
        Integer elapsedSeconds,
        SubmissionReason submissionReason,
        ScoreStatus scoreStatus,
        AttemptTimerResponse timer,
        List<AttemptQuestionResponse> questions
) {
}
