package com.antarang.cap.dto.response;

import java.util.UUID;

public record LanguageResponse(
        UUID id,
        String code,
        String name,
        String nativeName,
        boolean isDefault
) {
}
