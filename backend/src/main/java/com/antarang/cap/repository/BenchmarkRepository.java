package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.Benchmark;
import com.antarang.cap.domain.enums.BenchmarkLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BenchmarkRepository extends JpaRepository<Benchmark, UUID> {
    Optional<Benchmark> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("""
            SELECT b FROM Benchmark b
            WHERE b.tenant.id = :tenantId
              AND (:assessmentId IS NULL OR b.assessment.id = :assessmentId)
              AND (:domainCode IS NULL OR b.domainCode = :domainCode)
              AND (:gradeConfigId IS NULL OR b.gradeConfig.id = :gradeConfigId)
              AND (:ageGroupConfigId IS NULL OR b.ageGroupConfig.id = :ageGroupConfigId)
              AND (:benchmarkLabel IS NULL OR b.benchmarkLabel = :benchmarkLabel)
              AND (:isActive IS NULL OR b.isActive = :isActive)
            ORDER BY b.domainCode ASC, b.minScore ASC
            """)
    List<Benchmark> findWithFilters(
            @Param("tenantId") UUID tenantId,
            @Param("assessmentId") UUID assessmentId,
            @Param("domainCode") String domainCode,
            @Param("gradeConfigId") UUID gradeConfigId,
            @Param("ageGroupConfigId") UUID ageGroupConfigId,
            @Param("benchmarkLabel") BenchmarkLabel benchmarkLabel,
            @Param("isActive") Boolean isActive
    );
}
