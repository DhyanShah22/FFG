package com.antarang.cap.dto.request;

import java.math.BigDecimal;
import java.util.Map;

public record UpdateCareerMappingRequest(
        String interestDomain,
        String aptitudeDomain,
        String realityFactor,
        String aspirationFactor,
        BigDecimal minScore,
        BigDecimal maxScore,
        BigDecimal mappingWeight,
        Map<String, Object> mappingConfig,
        Boolean isActive
) {
}
