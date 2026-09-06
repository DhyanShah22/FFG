package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.ScoreStatus;
import com.antarang.cap.domain.enums.SubmissionReason;

import java.time.Instant;
import java.util.UUID;

public record AttemptHistoryItemResponse(
        UUID attemptId,
        UUID assessmentId,
        String assessmentCode,
        String assessmentName,
        int attemptNumber,
        AttemptStatus status,
        ScoreStatus scoreStatus,
        Instant startedAt,
        Instant submittedAt,
        Integer elapsedSeconds,
        SubmissionReason submissionReason
) {
}
