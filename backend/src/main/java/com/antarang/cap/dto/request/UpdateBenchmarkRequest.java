package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.BenchmarkLabel;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateBenchmarkRequest(
        String domainCode,
        UUID gradeConfigId,
        UUID ageGroupConfigId,
        BigDecimal minScore,
        BigDecimal maxScore,
        BenchmarkLabel benchmarkLabel,
        String interpretation,
        Boolean isActive
) {
}
