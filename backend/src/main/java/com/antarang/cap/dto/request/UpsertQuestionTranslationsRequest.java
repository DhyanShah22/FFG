package com.antarang.cap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpsertQuestionTranslationsRequest(
        @Valid List<QuestionTranslationItem> translations,
        @Valid List<OptionTranslationItem> optionTranslations
) {
    public record QuestionTranslationItem(
            @NotNull UUID languageId,
            @NotBlank String questionText,
            String helpText
    ) {
    }

    public record OptionTranslationItem(
            @NotNull UUID optionId,
            @NotNull UUID languageId,
            @NotBlank String optionText
    ) {
    }
}
