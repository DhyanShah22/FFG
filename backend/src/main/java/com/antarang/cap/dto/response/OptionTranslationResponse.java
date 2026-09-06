package com.antarang.cap.dto.response;

import java.util.UUID;

public record OptionTranslationResponse(
        UUID id,
        UUID optionId,
        UUID languageId,
        String languageCode,
        String optionText
) {
}
