package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record CreateCareerMappingRequest(
        @NotNull UUID careerId,
        String interestDomain,
        String aptitudeDomain,
        String realityFactor,
        String aspirationFactor,
        BigDecimal minScore,
        BigDecimal maxScore,
        BigDecimal mappingWeight,
        Map<String, Object> mappingConfig
) {
}
