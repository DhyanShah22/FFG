package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReorderQuestionnaireQuestionsRequest(
        @NotEmpty List<UUID> questionIds
) {
}
