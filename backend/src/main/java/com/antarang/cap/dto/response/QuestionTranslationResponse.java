package com.antarang.cap.dto.response;

import java.util.UUID;

public record QuestionTranslationResponse(
        UUID id,
        UUID languageId,
        String languageCode,
        String questionText,
        String helpText
) {
}
