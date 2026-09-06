package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record QuestionTimingItemRequest(
        @NotNull UUID questionId,
        Instant startedAt,
        Instant endedAt,
        Integer elapsedSeconds
) {
}
