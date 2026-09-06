package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.BenchmarkLabel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BenchmarkResponse(
        UUID id,
        UUID tenantId,
        UUID assessmentId,
        String domainCode,
        UUID gradeConfigId,
        UUID ageGroupConfigId,
        BigDecimal minScore,
        BigDecimal maxScore,
        BenchmarkLabel benchmarkLabel,
        String interpretation,
        boolean isActive,
        Instant createdAt
) {
}
