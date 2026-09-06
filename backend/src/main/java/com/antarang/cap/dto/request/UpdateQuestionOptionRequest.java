package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record UpdateQuestionOptionRequest(
        UUID id,
        @NotBlank String optionCode,
        @NotBlank String defaultText,
        BigDecimal scoreValue,
        Integer displayOrder,
        Map<String, Object> metadata,
        Boolean isActive
) {
}
