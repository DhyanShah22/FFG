package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateUserLanguageRequest(
        @NotNull UUID languageId
) {
}
