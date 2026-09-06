package com.antarang.cap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AddAssessmentConfigurationGroupItemsRequest(
        @NotEmpty @Valid List<Item> items
) {
    public record Item(
            @NotNull UUID assessmentId,
            @NotNull UUID assessmentVersionId,
            @NotNull UUID questionnaireVersionId,
            UUID iarLogicVersionId,
            UUID scoringRuleVersionId,
            @NotNull UUID assessmentLanguageId,
            Boolean isRequired,
            Integer displayOrder
    ) {
    }
}
