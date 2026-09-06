package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.ScoreStatus;
import com.antarang.cap.domain.enums.SubmissionReason;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubmitAttemptResponse(
        UUID attemptId,
        AttemptStatus status,
        Instant startedAt,
        Instant submittedAt,
        Integer elapsedSeconds,
        SubmissionReason submissionReason,
        ScoreStatus scoreStatus,
        List<DomainScoreResponse> domainScores
) {
}
