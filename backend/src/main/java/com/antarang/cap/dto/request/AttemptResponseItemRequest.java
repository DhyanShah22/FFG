package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AttemptResponseItemRequest(
        @NotNull UUID questionId,
        List<UUID> selectedOptionIds,
        String responseText,
        BigDecimal responseNumeric,
        Boolean isSkipped
) {
}
