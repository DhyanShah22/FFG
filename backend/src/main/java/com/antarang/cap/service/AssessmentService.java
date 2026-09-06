package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.Assessment;
import com.antarang.cap.domain.entity.AssessmentConfiguration;
import com.antarang.cap.domain.entity.AssessmentVersion;
import com.antarang.cap.domain.entity.QuestionnaireVersion;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.CompletionRule;
import com.antarang.cap.domain.enums.TimerMode;
import com.antarang.cap.domain.enums.VersionStatus;
import com.antarang.cap.dto.request.CreateAssessmentRequest;
import com.antarang.cap.dto.request.CreateAssessmentVersionRequest;
import com.antarang.cap.dto.request.UpsertAssessmentConfigurationRequest;
import com.antarang.cap.dto.response.AssessmentConfigurationResponse;
import com.antarang.cap.dto.response.AssessmentResponse;
import com.antarang.cap.dto.response.AssessmentVersionResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentConfigurationRepository;
import com.antarang.cap.repository.AssessmentRepository;
import com.antarang.cap.repository.AssessmentVersionRepository;
import com.antarang.cap.repository.QuestionnaireVersionRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentVersionRepository assessmentVersionRepository;
    private final AssessmentConfigurationRepository assessmentConfigurationRepository;
    private final QuestionnaireVersionRepository questionnaireVersionRepository;
    private final TenantRepository tenantRepository;

    public AssessmentService(
            AssessmentRepository assessmentRepository,
            AssessmentVersionRepository assessmentVersionRepository,
            AssessmentConfigurationRepository assessmentConfigurationRepository,
            QuestionnaireVersionRepository questionnaireVersionRepository,
            TenantRepository tenantRepository
    ) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentVersionRepository = assessmentVersionRepository;
        this.assessmentConfigurationRepository = assessmentConfigurationRepository;
        this.questionnaireVersionRepository = questionnaireVersionRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public AssessmentResponse create(CreateAssessmentRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AssessmentType code;
        try {
            code = AssessmentType.valueOf(request.code().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid assessment code", "VALIDATION_ERROR");
        }

        if (assessmentRepository.existsByTenantIdAndCodeAndIsDeletedFalse(tenantId, code)) {
            throw new BusinessException("Assessment code already exists for tenant", "DUPLICATE_RESOURCE");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Assessment assessment = new Assessment();
        assessment.setTenant(tenant);
        assessment.setCode(code);
        assessment.setName(request.name());
        assessment.setDescription(request.description());
        return toResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public PageResponse<AssessmentResponse> list(int page, int size, String assessmentType) {
        UUID tenantId = SecurityUtils.requireTenantId();
        PageRequest pageable = PageRequest.of(page, size);
        if (assessmentType != null && !assessmentType.isBlank()) {
            try {
                AssessmentType type = AssessmentType.valueOf(assessmentType.trim().toUpperCase());
                return PageResponse.from(
                        assessmentRepository.findByTenantIdAndCodeAndIsDeletedFalse(tenantId, type, pageable)
                                .map(this::toResponse)
                );
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("Invalid assessment type", "VALIDATION_ERROR");
            }
        }
        return PageResponse.from(
                assessmentRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageable)
                        .map(this::toResponse)
        );
    }

    @Transactional
    public AssessmentVersionResponse createVersion(UUID assessmentId, CreateAssessmentVersionRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Assessment assessment = assessmentRepository.findByIdAndTenantIdAndIsDeletedFalse(assessmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        QuestionnaireVersion questionnaireVersion = questionnaireVersionRepository
                .findByIdAndQuestionnaireTenantId(request.questionnaireVersionId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire version not found"));

        if (questionnaireVersion.getStatus() != VersionStatus.PUBLISHED) {
            throw new BusinessException("Questionnaire version must be PUBLISHED", "INVALID_STATE");
        }

        int versionNumber = request.versionNumber() != null
                ? request.versionNumber()
                : assessmentVersionRepository.findTopByAssessmentIdOrderByVersionNumberDesc(assessmentId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        AssessmentVersion version = new AssessmentVersion();
        version.setAssessment(assessment);
        version.setQuestionnaireVersion(questionnaireVersion);
        version.setVersionNumber(versionNumber);
        version.setStatus(VersionStatus.DRAFT);
        return toVersionResponse(assessmentVersionRepository.save(version));
    }

    @Transactional
    public AssessmentConfigurationResponse upsertConfiguration(UUID versionId, UpsertAssessmentConfigurationRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AssessmentVersion version = assessmentVersionRepository.findByIdAndAssessmentTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment version not found"));

        AssessmentConfiguration configuration = assessmentConfigurationRepository
                .findByAssessmentVersionId(versionId)
                .orElseGet(() -> {
                    AssessmentConfiguration created = new AssessmentConfiguration();
                    created.setAssessmentVersion(version);
                    created.setTimerMode(TimerMode.COUNT_UP);
                    return created;
                });

        if (request.timerMode() != null) {
            configuration.setTimerMode(request.timerMode());
        } else if (configuration.getId() == null) {
            configuration.setTimerMode(TimerMode.COUNT_UP);
        }
        if (request.maxDurationMinutes() != null || configuration.getId() != null) {
            configuration.setMaxDurationMinutes(request.maxDurationMinutes());
        }
        if (request.autoSubmitEnabled() != null) {
            configuration.setAutoSubmitEnabled(request.autoSubmitEnabled());
        }
        if (request.showTimerToStudent() != null) {
            configuration.setShowTimerToStudent(request.showTimerToStudent());
        }
        if (request.allowResume() != null) {
            configuration.setAllowResume(request.allowResume());
        }
        if (request.restartOnInterruption() != null) {
            configuration.setRestartOnInterruption(request.restartOnInterruption());
        }
        if (request.allowReattempt() != null) {
            configuration.setAllowReattempt(request.allowReattempt());
        }
        if (request.reattemptAfterDays() != null || configuration.getId() != null) {
            configuration.setReattemptAfterDays(request.reattemptAfterDays());
        }
        if (request.maxAttempts() != null) {
            configuration.setMaxAttempts(request.maxAttempts());
        }
        if (request.completionRule() != null) {
            configuration.setCompletionRule(request.completionRule());
        } else if (configuration.getId() == null) {
            configuration.setCompletionRule(CompletionRule.ALL_MANDATORY);
        }
        if (request.configJson() != null) {
            configuration.setConfigJson(request.configJson());
        }

        return toConfigurationResponse(assessmentConfigurationRepository.save(configuration));
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getTenant().getId(),
                assessment.getCode(),
                assessment.getName(),
                assessment.getDescription(),
                assessment.isActive()
        );
    }

    private AssessmentVersionResponse toVersionResponse(AssessmentVersion version) {
        return new AssessmentVersionResponse(
                version.getId(),
                version.getAssessment().getId(),
                version.getQuestionnaireVersion().getId(),
                version.getVersionNumber(),
                version.getStatus(),
                version.getPublishedAt(),
                version.getCreatedAt()
        );
    }

    private AssessmentConfigurationResponse toConfigurationResponse(AssessmentConfiguration configuration) {
        return new AssessmentConfigurationResponse(
                configuration.getId(),
                configuration.getAssessmentVersion().getId(),
                configuration.getTimerMode(),
                configuration.getMaxDurationMinutes(),
                configuration.isAutoSubmitEnabled(),
                configuration.isShowTimerToStudent(),
                configuration.isAllowResume(),
                configuration.isRestartOnInterruption(),
                configuration.isAllowReattempt(),
                configuration.getReattemptAfterDays(),
                configuration.getMaxAttempts(),
                configuration.getCompletionRule(),
                configuration.getConfigJson(),
                configuration.getCreatedAt()
        );
    }
}
