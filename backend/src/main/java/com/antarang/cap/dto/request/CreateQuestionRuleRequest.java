package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.RuleType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateQuestionRuleRequest(
        @NotNull RuleType ruleType,
        @NotNull Map<String, Object> ruleConfig
) {
}
