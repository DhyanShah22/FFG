package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Map;

public record CreateQuestionOptionRequest(
        @NotBlank String optionCode,
        @NotBlank String defaultText,
        BigDecimal scoreValue,
        Integer displayOrder,
        Map<String, Object> metadata
) {
}
