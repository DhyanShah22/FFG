package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.GeneratedReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, UUID> {
    Page<GeneratedReport> findByStudentIdOrderByGeneratedAtDesc(UUID studentId, Pageable pageable);

    @Query("""
            SELECT gr FROM GeneratedReport gr
            WHERE gr.student.id = :studentId
            ORDER BY gr.generatedAt DESC
            """)
    Page<GeneratedReport> findByStudentId(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("""
            SELECT gr FROM GeneratedReport gr
            JOIN gr.recommendationRun rr
            WHERE gr.student.id = :studentId
              AND rr.configurationGroup.id = :configurationGroupId
            ORDER BY gr.generatedAt DESC
            """)
    Page<GeneratedReport> findByStudentIdAndConfigurationGroupId(
            @Param("studentId") UUID studentId,
            @Param("configurationGroupId") UUID configurationGroupId,
            Pageable pageable
    );

    Optional<GeneratedReport> findTopByStudentIdOrderByGeneratedAtDesc(UUID studentId);

    long countByStudentId(UUID studentId);

    @Query("""
            SELECT COUNT(gr) FROM GeneratedReport gr
            JOIN gr.student s
            WHERE s.tenant.id = :tenantId
            """)
    long countByTenantId(@Param("tenantId") UUID tenantId);
}
