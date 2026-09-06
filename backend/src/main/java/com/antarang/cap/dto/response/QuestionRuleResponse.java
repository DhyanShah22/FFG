package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.RuleType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record QuestionRuleResponse(
        UUID id,
        UUID questionId,
        RuleType ruleType,
        Map<String, Object> ruleConfig,
        boolean isActive,
        Instant createdAt
) {
}
