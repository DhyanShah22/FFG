package com.antarang.cap.service;

import com.antarang.cap.domain.entity.ReportSection;
import com.antarang.cap.domain.entity.ReportTemplate;
import com.antarang.cap.domain.entity.ReportTemplateVersion;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.VersionStatus;
import com.antarang.cap.dto.request.CreateReportTemplateRequest;
import com.antarang.cap.dto.request.CreateReportTemplateVersionRequest;
import com.antarang.cap.dto.request.ReportSectionRequest;
import com.antarang.cap.dto.request.UpdateReportTemplateVersionRequest;
import com.antarang.cap.dto.response.ReportSectionResponse;
import com.antarang.cap.dto.response.ReportTemplateResponse;
import com.antarang.cap.dto.response.ReportTemplateVersionResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.ReportSectionRepository;
import com.antarang.cap.repository.ReportTemplateRepository;
import com.antarang.cap.repository.ReportTemplateVersionRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportTemplateService {

    private final ReportTemplateRepository templateRepository;
    private final ReportTemplateVersionRepository versionRepository;
    private final ReportSectionRepository sectionRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;

    public ReportTemplateService(
            ReportTemplateRepository templateRepository,
            ReportTemplateVersionRepository versionRepository,
            ReportSectionRepository sectionRepository,
            TenantRepository tenantRepository,
            AuditService auditService
    ) {
        this.templateRepository = templateRepository;
        this.versionRepository = versionRepository;
        this.sectionRepository = sectionRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ReportTemplateResponse create(CreateReportTemplateRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        if (templateRepository.existsByTenantIdAndCode(tenantId, request.code())) {
            throw new BusinessException("Report template code already exists", "DUPLICATE_RESOURCE");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        ReportTemplate template = new ReportTemplate();
        template.setTenant(tenant);
        template.setCode(request.code());
        template.setName(request.name());
        template.setTemplateType(request.templateType());
        template.setStatus(LifecycleStatus.DRAFT);
        ReportTemplate savedTemplate = templateRepository.save(template);

        ReportTemplateVersion version = new ReportTemplateVersion();
        version.setReportTemplate(savedTemplate);
        version.setVersionNumber(1);
        version.setTemplateConfig(new HashMap<>());
        version.setStatus(VersionStatus.DRAFT);
        ReportTemplateVersion savedVersion = versionRepository.save(version);

        savedTemplate.setCurrentVersionId(savedVersion.getId());
        templateRepository.save(savedTemplate);

        return toTemplateResponse(savedTemplate, VersionStatus.DRAFT);
    }

    @Transactional(readOnly = true)
    public List<ReportTemplateResponse> list() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return templateRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .map(template -> {
                    VersionStatus latest = versionRepository
                            .findTopByReportTemplateIdOrderByVersionNumberDesc(template.getId())
                            .map(ReportTemplateVersion::getStatus)
                            .orElse(null);
                    return toTemplateResponse(template, latest);
                })
                .toList();
    }

    @Transactional
    public ReportTemplateVersionResponse createVersion(UUID templateId, CreateReportTemplateVersionRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        ReportTemplate template = templateRepository.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Report template not found"));

        int versionNumber = versionRepository.findTopByReportTemplateIdOrderByVersionNumberDesc(templateId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        ReportTemplateVersion version = new ReportTemplateVersion();
        version.setReportTemplate(template);
        version.setVersionNumber(versionNumber);
        version.setTemplateConfig(request.templateConfig() != null ? request.templateConfig() : Map.of());
        version.setVersionNotes(request.versionNotes());
        version.setStatus(VersionStatus.DRAFT);
        return toVersionResponse(versionRepository.save(version));
    }

    @Transactional
    public ReportTemplateVersionResponse updateVersion(UUID versionId, UpdateReportTemplateVersionRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        ReportTemplateVersion version = versionRepository.findByIdAndReportTemplateTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Report template version not found"));

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT report template versions can be updated", "INVALID_STATE");
        }

        if (request.templateConfig() != null) {
            version.setTemplateConfig(request.templateConfig());
        }

        if (request.sections() != null) {
            sectionRepository.deleteByReportTemplateVersionId(versionId);
            for (ReportSectionRequest sectionRequest : request.sections()) {
                ReportSection section = new ReportSection();
                section.setReportTemplateVersion(version);
                section.setSectionCode(sectionRequest.sectionCode());
                section.setSectionTitle(sectionRequest.sectionTitle());
                section.setDisplayOrder(sectionRequest.displayOrder());
                section.setSectionConfig(sectionRequest.sectionConfig());
                sectionRepository.save(section);
            }
        }

        return toVersionResponse(versionRepository.save(version));
    }

    @Transactional(readOnly = true)
    public List<ReportSectionResponse> listSections(UUID versionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        versionRepository.findByIdAndReportTemplateTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Report template version not found"));
        return sectionRepository.findByReportTemplateVersionIdOrderByDisplayOrderAsc(versionId).stream()
                .map(s -> new ReportSectionResponse(
                        s.getId(),
                        s.getSectionCode(),
                        s.getSectionTitle(),
                        s.getDisplayOrder(),
                        s.getSectionConfig()
                ))
                .toList();
    }

    @Transactional
    public ReportTemplateVersionResponse publish(UUID versionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UUID userId = SecurityUtils.requirePrincipal().getId();
        ReportTemplateVersion version = versionRepository.findByIdAndReportTemplateTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Report template version not found"));

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT report template versions can be published", "INVALID_STATE");
        }

        version.setStatus(VersionStatus.PUBLISHED);
        version.setPublishedAt(Instant.now());
        version.setPublishedBy(userId);
        ReportTemplateVersion saved = versionRepository.save(version);

        ReportTemplate template = saved.getReportTemplate();
        template.setCurrentVersionId(saved.getId());
        template.setStatus(LifecycleStatus.ACTIVE);
        templateRepository.save(template);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("versionId", saved.getId().toString());
        newValue.put("versionNumber", saved.getVersionNumber());
        auditService.record("report_template_versions", saved.getId(), AuditAction.PUBLISH, null, newValue);

        return toVersionResponse(saved);
    }

    private ReportTemplateResponse toTemplateResponse(ReportTemplate template, VersionStatus latestVersionStatus) {
        return new ReportTemplateResponse(
                template.getId(),
                template.getTenant().getId(),
                template.getCode(),
                template.getName(),
                template.getTemplateType(),
                template.getStatus(),
                template.getCurrentVersionId(),
                latestVersionStatus,
                template.isActive()
        );
    }

    private ReportTemplateVersionResponse toVersionResponse(ReportTemplateVersion version) {
        return new ReportTemplateVersionResponse(
                version.getId(),
                version.getReportTemplate().getId(),
                version.getVersionNumber(),
                version.getTemplateConfig(),
                version.getStatus(),
                version.getPublishedAt(),
                version.getPublishedBy(),
                version.getVersionNotes()
        );
    }
}
