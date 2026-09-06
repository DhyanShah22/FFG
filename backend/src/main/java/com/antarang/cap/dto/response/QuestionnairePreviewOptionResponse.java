package com.antarang.cap.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record QuestionnairePreviewOptionResponse(
        UUID optionId,
        String optionCode,
        String optionText,
        BigDecimal scoreValue,
        int displayOrder
) {
}
