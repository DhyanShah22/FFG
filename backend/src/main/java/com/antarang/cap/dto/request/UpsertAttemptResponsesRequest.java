package com.antarang.cap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpsertAttemptResponsesRequest(
        @NotEmpty @Valid List<AttemptResponseItemRequest> responses
) {
}
