package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ReportTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportTemplateVersionRepository extends JpaRepository<ReportTemplateVersion, UUID> {
    List<ReportTemplateVersion> findByReportTemplateIdOrderByVersionNumberAsc(UUID templateId);
    Optional<ReportTemplateVersion> findByIdAndReportTemplateTenantId(UUID id, UUID tenantId);
    Optional<ReportTemplateVersion> findTopByReportTemplateIdOrderByVersionNumberDesc(UUID templateId);
}
