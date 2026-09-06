package com.antarang.cap.dto.request;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UpsertQuestionTimingRequest(
        UUID questionId,
        Instant startedAt,
        Instant endedAt,
        Integer elapsedSeconds,
        @Valid List<QuestionTimingItemRequest> timings
) {
}
