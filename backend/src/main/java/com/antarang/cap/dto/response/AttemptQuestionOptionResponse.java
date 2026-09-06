package com.antarang.cap.dto.response;

import java.util.UUID;

public record AttemptQuestionOptionResponse(
        UUID optionId,
        String optionText,
        int displayOrder
) {
}
