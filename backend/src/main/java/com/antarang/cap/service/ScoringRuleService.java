package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Assessment;
import com.antarang.cap.domain.entity.ScoringRule;
import com.antarang.cap.domain.entity.ScoringRuleVersion;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.VersionStatus;
import com.antarang.cap.dto.request.CreateScoringRuleRequest;
import com.antarang.cap.dto.request.CreateScoringRuleVersionRequest;
import com.antarang.cap.dto.request.UpdateScoringRuleVersionRequest;
import com.antarang.cap.dto.response.ScoringRuleResponse;
import com.antarang.cap.dto.response.ScoringRuleVersionResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentRepository;
import com.antarang.cap.repository.ScoringRuleRepository;
import com.antarang.cap.repository.ScoringRuleVersionRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ScoringRuleService {

    private final ScoringRuleRepository scoringRuleRepository;
    private final ScoringRuleVersionRepository versionRepository;
    private final AssessmentRepository assessmentRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;

    public ScoringRuleService(
            ScoringRuleRepository scoringRuleRepository,
            ScoringRuleVersionRepository versionRepository,
            AssessmentRepository assessmentRepository,
            TenantRepository tenantRepository,
            AuditService auditService
    ) {
        this.scoringRuleRepository = scoringRuleRepository;
        this.versionRepository = versionRepository;
        this.assessmentRepository = assessmentRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ScoringRuleResponse create(CreateScoringRuleRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        if (scoringRuleRepository.existsByTenantIdAndCode(tenantId, request.code())) {
            throw new BusinessException("Scoring rule code already exists", "DUPLICATE_RESOURCE");
        }
        Assessment assessment = assessmentRepository.findByIdAndTenantIdAndIsDeletedFalse(request.assessmentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        ScoringRule rule = new ScoringRule();
        rule.setTenant(tenant);
        rule.setAssessment(assessment);
        rule.setCode(request.code());
        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setStatus(LifecycleStatus.DRAFT);
        return toRuleResponse(scoringRuleRepository.save(rule), null);
    }

    @Transactional(readOnly = true)
    public List<ScoringRuleResponse> list() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return scoringRuleRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(rule -> {
                    VersionStatus latest = versionRepository
                            .findTopByScoringRuleIdOrderByVersionNumberDesc(rule.getId())
                            .map(ScoringRuleVersion::getStatus)
                            .orElse(null);
                    return toRuleResponse(rule, latest);
                })
                .toList();
    }

    @Transactional
    public ScoringRuleVersionResponse createVersion(UUID scoringRuleId, CreateScoringRuleVersionRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        ScoringRule rule = scoringRuleRepository.findByIdAndTenantId(scoringRuleId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Scoring rule not found"));

        int versionNumber = request.versionNumber() != null
                ? request.versionNumber()
                : versionRepository.findTopByScoringRuleIdOrderByVersionNumberDesc(scoringRuleId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        ScoringRuleVersion version = new ScoringRuleVersion();
        version.setScoringRule(rule);
        version.setVersionNumber(versionNumber);
        version.setRuleDefinition(request.ruleDefinition());
        version.setVersionNotes(request.versionNotes());
        version.setStatus(VersionStatus.DRAFT);

        ScoringRuleVersion saved = versionRepository.save(version);
        if (rule.getCurrentVersionId() == null) {
            rule.setCurrentVersionId(saved.getId());
            rule.setUpdatedAt(Instant.now());
            scoringRuleRepository.save(rule);
        }
        return toVersionResponse(saved);
    }

    @Transactional
    public ScoringRuleVersionResponse updateVersion(UUID versionId, UpdateScoringRuleVersionRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        ScoringRuleVersion version = versionRepository.findByIdAndScoringRuleTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Scoring rule version not found"));
        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT scoring rule versions can be updated", "INVALID_STATE");
        }
        version.setRuleDefinition(request.ruleDefinition());
        if (request.versionNotes() != null) {
            version.setVersionNotes(request.versionNotes());
        }
        return toVersionResponse(versionRepository.save(version));
    }

    @Transactional
    public ScoringRuleVersionResponse publish(UUID versionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UUID userId = SecurityUtils.requirePrincipal().getId();
        ScoringRuleVersion version = versionRepository.findByIdAndScoringRuleTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Scoring rule version not found"));

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT scoring rule versions can be published", "INVALID_STATE");
        }
        if (version.getRuleDefinition() == null || version.getRuleDefinition().isEmpty()) {
            throw new BusinessException("ruleDefinition is required", "VALIDATION_ERROR");
        }

        version.setStatus(VersionStatus.PUBLISHED);
        version.setPublishedAt(Instant.now());
        version.setPublishedBy(userId);
        ScoringRuleVersion saved = versionRepository.save(version);

        ScoringRule rule = saved.getScoringRule();
        rule.setCurrentVersionId(saved.getId());
        rule.setStatus(LifecycleStatus.ACTIVE);
        rule.setUpdatedAt(Instant.now());
        scoringRuleRepository.save(rule);

        auditService.record(
                "ScoringRuleVersion",
                saved.getId(),
                AuditAction.PUBLISH,
                Map.of("status", VersionStatus.DRAFT.name()),
                Map.of("status", VersionStatus.PUBLISHED.name(), "versionNumber", saved.getVersionNumber())
        );
        return toVersionResponse(saved);
    }

    @Transactional
    public ScoringRuleVersionResponse retire(UUID versionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        ScoringRuleVersion version = versionRepository.findByIdAndScoringRuleTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Scoring rule version not found"));

        if (version.getStatus() == VersionStatus.ARCHIVED) {
            throw new BusinessException("Scoring rule version is already retired", "INVALID_STATE");
        }

        Map<String, Object> oldValue = Map.of("status", version.getStatus().name());
        version.setStatus(VersionStatus.ARCHIVED);
        ScoringRuleVersion saved = versionRepository.save(version);

        auditService.record(
                "ScoringRuleVersion",
                saved.getId(),
                AuditAction.STATUS_CHANGE,
                oldValue,
                Map.of("status", VersionStatus.ARCHIVED.name(), "action", "retire")
        );
        return toVersionResponse(saved);
    }

    private ScoringRuleResponse toRuleResponse(ScoringRule rule, VersionStatus latestVersionStatus) {
        return new ScoringRuleResponse(
                rule.getId(),
                rule.getTenant().getId(),
                rule.getAssessment().getId(),
                rule.getCode(),
                rule.getName(),
                rule.getDescription(),
                rule.getStatus(),
                rule.getCurrentVersionId(),
                latestVersionStatus,
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    private ScoringRuleVersionResponse toVersionResponse(ScoringRuleVersion version) {
        return new ScoringRuleVersionResponse(
                version.getId(),
                version.getScoringRule().getId(),
                version.getVersionNumber(),
                version.getRuleDefinition(),
                version.getStatus(),
                version.getPublishedAt(),
                version.getPublishedBy(),
                version.getVersionNotes(),
                version.getCreatedAt()
        );
    }
}
