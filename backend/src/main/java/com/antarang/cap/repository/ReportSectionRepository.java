package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ReportSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportSectionRepository extends JpaRepository<ReportSection, UUID> {
    List<ReportSection> findByReportTemplateVersionIdOrderByDisplayOrderAsc(UUID reportTemplateVersionId);
    void deleteByReportTemplateVersionId(UUID reportTemplateVersionId);
}
