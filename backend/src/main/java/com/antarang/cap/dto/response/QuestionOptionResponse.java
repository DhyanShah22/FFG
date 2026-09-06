package com.antarang.cap.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuestionOptionResponse(
        UUID id,
        String optionCode,
        String defaultText,
        BigDecimal scoreValue,
        int displayOrder,
        Map<String, Object> metadata,
        boolean isActive,
        List<OptionTranslationResponse> translations
) {
}
