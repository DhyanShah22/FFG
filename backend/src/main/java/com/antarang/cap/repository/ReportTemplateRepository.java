package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, UUID> {
    Optional<ReportTemplate> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCode(UUID tenantId, String code);
    List<ReportTemplate> findByTenantIdOrderByCodeAsc(UUID tenantId);
}
