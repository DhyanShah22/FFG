package com.antarang.cap.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record CareerMappingResponse(
        UUID id,
        UUID tenantId,
        UUID careerId,
        String interestDomain,
        String aptitudeDomain,
        String realityFactor,
        String aspirationFactor,
        BigDecimal minScore,
        BigDecimal maxScore,
        BigDecimal mappingWeight,
        Map<String, Object> mappingConfig,
        boolean isActive
) {
}
