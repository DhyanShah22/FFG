package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.OptionTranslation;
import com.antarang.cap.domain.entity.Question;
import com.antarang.cap.domain.entity.QuestionOption;
import com.antarang.cap.domain.entity.QuestionTranslation;
import com.antarang.cap.domain.entity.Questionnaire;
import com.antarang.cap.domain.entity.QuestionnaireQuestion;
import com.antarang.cap.domain.entity.QuestionnaireVersion;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.ReviewStatus;
import com.antarang.cap.domain.enums.VersionStatus;
import com.antarang.cap.dto.request.CloneQuestionnaireRequest;
import com.antarang.cap.dto.request.CreateQuestionnaireRequest;
import com.antarang.cap.dto.request.CreateQuestionnaireVersionRequest;
import com.antarang.cap.dto.request.PublishQuestionnaireVersionRequest;
import com.antarang.cap.dto.request.QuestionnaireQuestionItemRequest;
import com.antarang.cap.dto.request.ReorderQuestionnaireQuestionsRequest;
import com.antarang.cap.dto.request.SetQuestionnaireQuestionsRequest;
import com.antarang.cap.dto.request.UpdateQuestionnaireRequest;
import com.antarang.cap.dto.response.QuestionnaireListItemResponse;
import com.antarang.cap.dto.response.QuestionnairePreviewOptionResponse;
import com.antarang.cap.dto.response.QuestionnairePreviewQuestionResponse;
import com.antarang.cap.dto.response.QuestionnairePreviewResponse;
import com.antarang.cap.dto.response.QuestionnaireQuestionSummaryResponse;
import com.antarang.cap.dto.response.QuestionnaireResponse;
import com.antarang.cap.dto.response.QuestionnaireVersionSummaryResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.OptionTranslationRepository;
import com.antarang.cap.repository.QuestionOptionRepository;
import com.antarang.cap.repository.QuestionRepository;
import com.antarang.cap.repository.QuestionTranslationRepository;
import com.antarang.cap.repository.QuestionnaireQuestionRepository;
import com.antarang.cap.repository.QuestionnaireRepository;
import com.antarang.cap.repository.QuestionnaireVersionRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireVersionRepository questionnaireVersionRepository;
    private final QuestionnaireQuestionRepository questionnaireQuestionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionTranslationRepository questionTranslationRepository;
    private final OptionTranslationRepository optionTranslationRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;

    public QuestionnaireService(
            QuestionnaireRepository questionnaireRepository,
            QuestionnaireVersionRepository questionnaireVersionRepository,
            QuestionnaireQuestionRepository questionnaireQuestionRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            QuestionTranslationRepository questionTranslationRepository,
            OptionTranslationRepository optionTranslationRepository,
            TenantRepository tenantRepository,
            AuditService auditService
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionnaireVersionRepository = questionnaireVersionRepository;
        this.questionnaireQuestionRepository = questionnaireQuestionRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.questionTranslationRepository = questionTranslationRepository;
        this.optionTranslationRepository = optionTranslationRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    @Transactional
    public QuestionnaireResponse create(CreateQuestionnaireRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        if (questionnaireRepository.existsByTenantIdAndCodeAndIsDeletedFalse(tenantId, request.code())) {
            throw new BusinessException("Questionnaire code already exists", "DUPLICATE_RESOURCE");
        }

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTenant(tenant);
        questionnaire.setAssessmentType(request.assessmentType());
        questionnaire.setCode(request.code());
        questionnaire.setName(request.name());
        questionnaire.setDescription(request.description());
        questionnaire.setShuffleQuestions(Boolean.TRUE.equals(request.shuffleQuestions()));
        questionnaire.setStatus(LifecycleStatus.DRAFT);
        questionnaire = questionnaireRepository.save(questionnaire);

        QuestionnaireVersion version = new QuestionnaireVersion();
        version.setQuestionnaire(questionnaire);
        version.setVersionNumber(1);
        version.setStatus(VersionStatus.DRAFT);
        version = questionnaireVersionRepository.save(version);

        questionnaire.setCurrentVersionId(version.getId());
        questionnaireRepository.save(questionnaire);

        return toResponse(questionnaire, version, List.of());
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionnaireListItemResponse> list(AssessmentType assessmentType, int page, int size) {
        UUID tenantId = SecurityUtils.requireTenantId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        Page<Questionnaire> result = assessmentType != null
                ? questionnaireRepository.findByTenantIdAndAssessmentTypeAndIsDeletedFalse(tenantId, assessmentType, pageable)
                : questionnaireRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageable);
        return PageResponse.from(result.map(this::toListItem));
    }

    @Transactional(readOnly = true)
    public QuestionnaireResponse getById(UUID id, UUID versionId) {
        Questionnaire questionnaire = requireQuestionnaire(id);
        QuestionnaireVersion version = resolveVersion(questionnaire, versionId);
        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());
        return toResponse(questionnaire, version, questions);
    }

    @Transactional
    public QuestionnaireResponse update(UUID id, UpdateQuestionnaireRequest request) {
        Questionnaire questionnaire = requireQuestionnaire(id);
        QuestionnaireVersion latest = requireLatestVersion(questionnaire);

        if (request.name() != null && !request.name().isBlank()) {
            questionnaire.setName(request.name());
        }
        if (request.description() != null) {
            questionnaire.setDescription(request.description());
        }
        if (request.shuffleQuestions() != null) {
            questionnaire.setShuffleQuestions(request.shuffleQuestions());
        }

        QuestionnaireVersion targetVersion;
        if (latest.getStatus() == VersionStatus.DRAFT) {
            targetVersion = latest;
        } else if (latest.getStatus() == VersionStatus.PUBLISHED) {
            targetVersion = createNextDraftVersion(questionnaire, latest, null);
            questionnaire.setCurrentVersionId(targetVersion.getId());
        } else {
            throw new BusinessException(
                    "Cannot edit questionnaire when latest version is " + latest.getStatus(),
                    "INVALID_STATE"
            );
        }

        if (request.questions() != null) {
            replaceQuestions(targetVersion, request.questions());
        }

        questionnaireRepository.save(questionnaire);
        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(targetVersion.getId());
        return toResponse(questionnaire, targetVersion, questions);
    }

    @Transactional
    public QuestionnaireVersionSummaryResponse createVersion(UUID id, CreateQuestionnaireVersionRequest request) {
        Questionnaire questionnaire = requireQuestionnaire(id);
        QuestionnaireVersion latest = requireLatestVersion(questionnaire);
        if (latest.getStatus() == VersionStatus.DRAFT) {
            throw new BusinessException("A DRAFT version already exists; publish or edit it first", "INVALID_STATE");
        }

        QuestionnaireVersion version = createNextDraftVersion(
                questionnaire,
                latest,
                request != null ? request.versionNotes() : null
        );
        questionnaire.setCurrentVersionId(version.getId());
        questionnaireRepository.save(questionnaire);

        long count = questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId()).size();
        return new QuestionnaireVersionSummaryResponse(
                version.getId(),
                version.getVersionNumber(),
                version.getStatus(),
                version.getPublishedAt(),
                count
        );
    }

    @Transactional(readOnly = true)
    public List<QuestionnaireVersionSummaryResponse> listVersions(UUID id) {
        Questionnaire questionnaire = requireQuestionnaire(id);
        return questionnaireVersionRepository.findByQuestionnaireIdOrderByVersionNumberAsc(questionnaire.getId())
                .stream()
                .map(version -> {
                    long count = questionnaireQuestionRepository
                            .findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId())
                            .size();
                    return new QuestionnaireVersionSummaryResponse(
                            version.getId(),
                            version.getVersionNumber(),
                            version.getStatus(),
                            version.getPublishedAt(),
                            count
                    );
                })
                .toList();
    }

    @Transactional
    public QuestionnaireResponse clone(UUID id, CloneQuestionnaireRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Questionnaire source = requireQuestionnaire(id);

        QuestionnaireVersion sourceVersion = null;
        if (source.getCurrentVersionId() != null) {
            sourceVersion = questionnaireVersionRepository.findById(source.getCurrentVersionId()).orElse(null);
        }
        if (sourceVersion == null) {
            sourceVersion = requireLatestVersion(source);
        }

        String code = request != null && request.code() != null && !request.code().isBlank()
                ? request.code()
                : source.getCode() + "_CLONE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String name = request != null && request.name() != null && !request.name().isBlank()
                ? request.name()
                : source.getName() + " (Clone)";

        if (questionnaireRepository.existsByTenantIdAndCodeAndIsDeletedFalse(tenantId, code)) {
            throw new BusinessException("Questionnaire code already exists", "DUPLICATE_RESOURCE");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Questionnaire clone = new Questionnaire();
        clone.setTenant(tenant);
        clone.setAssessmentType(source.getAssessmentType());
        clone.setCode(code);
        clone.setName(name);
        clone.setDescription(source.getDescription());
        clone.setShuffleQuestions(source.isShuffleQuestions());
        clone.setStatus(LifecycleStatus.DRAFT);
        clone = questionnaireRepository.save(clone);

        QuestionnaireVersion version = new QuestionnaireVersion();
        version.setQuestionnaire(clone);
        version.setVersionNumber(1);
        version.setStatus(VersionStatus.DRAFT);
        version = questionnaireVersionRepository.save(version);

        clone.setCurrentVersionId(version.getId());
        questionnaireRepository.save(clone);

        copyQuestions(sourceVersion, version);
        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());
        return toResponse(clone, version, questions);
    }

    @Transactional
    public QuestionnaireResponse setQuestions(UUID versionId, SetQuestionnaireQuestionsRequest request) {
        QuestionnaireVersion version = requireVersion(versionId);
        assertDraftEditable(version);
        replaceQuestions(version, request.questions());
        Questionnaire questionnaire = version.getQuestionnaire();
        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());
        return toResponse(questionnaire, version, questions);
    }

    @Transactional
    public QuestionnaireResponse reorderQuestions(UUID versionId, ReorderQuestionnaireQuestionsRequest request) {
        QuestionnaireVersion version = requireVersion(versionId);
        assertDraftEditable(version);

        List<QuestionnaireQuestion> existing =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());
        Map<UUID, QuestionnaireQuestion> byQuestionId = existing.stream()
                .collect(Collectors.toMap(qq -> qq.getQuestion().getId(), Function.identity()));

        if (request.questionIds().size() != existing.size()) {
            throw new BusinessException("questionIds must include every question on the version", "VALIDATION_ERROR");
        }

        Set<UUID> seen = new HashSet<>();
        int order = 1;
        List<QuestionnaireQuestion> updated = new ArrayList<>();
        for (UUID questionId : request.questionIds()) {
            if (!seen.add(questionId)) {
                throw new BusinessException("Duplicate questionId in reorder list", "VALIDATION_ERROR");
            }
            QuestionnaireQuestion qq = byQuestionId.get(questionId);
            if (qq == null) {
                throw new BusinessException("Question not on this version: " + questionId, "VALIDATION_ERROR");
            }
            qq.setDisplayOrder(order++);
            updated.add(qq);
        }
        questionnaireQuestionRepository.saveAll(updated);

        Questionnaire questionnaire = version.getQuestionnaire();
        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());
        return toResponse(questionnaire, version, questions);
    }

    @Transactional(readOnly = true)
    public QuestionnairePreviewResponse preview(UUID versionId, UUID languageId) {
        QuestionnaireVersion version = requireVersion(versionId);
        Questionnaire questionnaire = version.getQuestionnaire();
        List<QuestionnaireQuestion> links =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());

        List<QuestionnairePreviewQuestionResponse> questions = new ArrayList<>();
        for (QuestionnaireQuestion link : links) {
            Question question = link.getQuestion();
            String questionText = question.getDefaultText();
            String helpText = question.getHelpText();
            if (languageId != null) {
                QuestionTranslation translation = questionTranslationRepository
                        .findByQuestionIdAndLanguageId(question.getId(), languageId)
                        .orElse(null);
                if (translation != null) {
                    questionText = translation.getQuestionText();
                    helpText = translation.getHelpText();
                }
            }

            List<QuestionOption> options =
                    questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId());
            List<QuestionnairePreviewOptionResponse> optionResponses = new ArrayList<>();
            for (QuestionOption option : options) {
                if (!option.isActive()) {
                    continue;
                }
                String optionText = option.getDefaultText();
                if (languageId != null) {
                    OptionTranslation ot = optionTranslationRepository
                            .findByOptionIdAndLanguageId(option.getId(), languageId)
                            .orElse(null);
                    if (ot != null) {
                        optionText = ot.getOptionText();
                    }
                }
                optionResponses.add(new QuestionnairePreviewOptionResponse(
                        option.getId(),
                        option.getOptionCode(),
                        optionText,
                        option.getScoreValue(),
                        option.getDisplayOrder()
                ));
            }

            questions.add(new QuestionnairePreviewQuestionResponse(
                    question.getId(),
                    question.getQuestionCode(),
                    question.getQuestionType(),
                    questionText,
                    helpText,
                    link.getDisplayOrder(),
                    link.isMandatory(),
                    link.getSectionCode(),
                    link.getSectionName(),
                    link.getConditionConfig(),
                    optionResponses
            ));
        }

        return new QuestionnairePreviewResponse(
                questionnaire.getId(),
                questionnaire.getCode(),
                questionnaire.getName(),
                questionnaire.getAssessmentType(),
                version.getId(),
                version.getVersionNumber(),
                version.getStatus(),
                languageId,
                questions
        );
    }

    @Transactional
    public QuestionnaireResponse publishVersion(UUID versionId, PublishQuestionnaireVersionRequest request) {
        QuestionnaireVersion version = requireVersion(versionId);
        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT versions can be published", "INVALID_STATE");
        }

        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());
        if (questions.isEmpty()) {
            throw new BusinessException("Cannot publish a version with no questions", "VALIDATION_ERROR");
        }

        Map<String, Object> oldValue = Map.of(
                "status", version.getStatus().name(),
                "questionnaireStatus", version.getQuestionnaire().getStatus().name()
        );

        UserPrincipal principal = SecurityUtils.requirePrincipal();
        if (request != null && request.versionNotes() != null && !request.versionNotes().isBlank()) {
            version.setVersionNotes(request.versionNotes());
        }
        version.setStatus(VersionStatus.PUBLISHED);
        version.setPublishedAt(Instant.now());
        version.setPublishedBy(principal.getId());
        questionnaireVersionRepository.save(version);

        Questionnaire questionnaire = version.getQuestionnaire();
        questionnaire.setStatus(LifecycleStatus.ACTIVE);
        questionnaire.setCurrentVersionId(version.getId());
        questionnaire.setActive(true);
        questionnaireRepository.save(questionnaire);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("status", VersionStatus.PUBLISHED.name());
        newValue.put("questionnaireStatus", LifecycleStatus.ACTIVE.name());
        newValue.put("publishedAt", version.getPublishedAt().toString());
        auditService.record("QuestionnaireVersion", version.getId(), AuditAction.PUBLISH, oldValue, newValue);

        return toResponse(questionnaire, version, questions);
    }

    @Transactional
    public QuestionnaireResponse publishCurrentDraft(UUID questionnaireId, PublishQuestionnaireVersionRequest request) {
        Questionnaire questionnaire = requireQuestionnaire(questionnaireId);
        QuestionnaireVersion latest = requireLatestVersion(questionnaire);
        if (latest.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException("Current latest version is not a DRAFT", "INVALID_STATE");
        }
        return publishVersion(latest.getId(), request);
    }

    @Transactional
    public QuestionnaireResponse suspendVersion(UUID versionId) {
        QuestionnaireVersion version = requireVersion(versionId);
        if (version.getStatus() != VersionStatus.PUBLISHED) {
            throw new BusinessException("Only PUBLISHED versions can be suspended", "INVALID_STATE");
        }

        Questionnaire questionnaire = version.getQuestionnaire();
        Map<String, Object> oldValue = Map.of(
                "versionStatus", version.getStatus().name(),
                "questionnaireStatus", questionnaire.getStatus().name()
        );

        questionnaire.setStatus(LifecycleStatus.INACTIVE);
        questionnaire.setActive(false);
        questionnaireRepository.save(questionnaire);

        Map<String, Object> newValue = Map.of(
                "versionStatus", version.getStatus().name(),
                "questionnaireStatus", LifecycleStatus.INACTIVE.name()
        );
        auditService.record("QuestionnaireVersion", version.getId(), AuditAction.STATUS_CHANGE, oldValue, newValue);

        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());
        return toResponse(questionnaire, version, questions);
    }

    @Transactional
    public QuestionnaireResponse retireVersion(UUID versionId) {
        QuestionnaireVersion version = requireVersion(versionId);
        if (version.getStatus() == VersionStatus.ARCHIVED) {
            throw new BusinessException("Version is already ARCHIVED", "INVALID_STATE");
        }

        Map<String, Object> oldValue = Map.of("status", version.getStatus().name());
        version.setStatus(VersionStatus.ARCHIVED);
        questionnaireVersionRepository.save(version);

        Map<String, Object> newValue = Map.of("status", VersionStatus.ARCHIVED.name());
        auditService.record("QuestionnaireVersion", version.getId(), AuditAction.STATUS_CHANGE, oldValue, newValue);

        Questionnaire questionnaire = version.getQuestionnaire();
        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(version.getId());
        return toResponse(questionnaire, version, questions);
    }

    private QuestionnaireVersion createNextDraftVersion(
            Questionnaire questionnaire,
            QuestionnaireVersion source,
            String versionNotes
    ) {
        QuestionnaireVersion version = new QuestionnaireVersion();
        version.setQuestionnaire(questionnaire);
        version.setVersionNumber(source.getVersionNumber() + 1);
        version.setStatus(VersionStatus.DRAFT);
        version.setVersionNotes(versionNotes);
        version = questionnaireVersionRepository.save(version);
        copyQuestions(source, version);
        return version;
    }

    private void copyQuestions(QuestionnaireVersion source, QuestionnaireVersion target) {
        List<QuestionnaireQuestion> sourceQuestions =
                questionnaireQuestionRepository.findByQuestionnaireVersionIdOrderByDisplayOrderAsc(source.getId());
        List<QuestionnaireQuestion> copies = new ArrayList<>();
        for (QuestionnaireQuestion sourceQq : sourceQuestions) {
            QuestionnaireQuestion copy = new QuestionnaireQuestion();
            copy.setQuestionnaireVersion(target);
            copy.setQuestion(sourceQq.getQuestion());
            copy.setDisplayOrder(sourceQq.getDisplayOrder());
            copy.setMandatory(sourceQq.isMandatory());
            copy.setSectionCode(sourceQq.getSectionCode());
            copy.setSectionName(sourceQq.getSectionName());
            copy.setConditionConfig(sourceQq.getConditionConfig());
            copies.add(copy);
        }
        if (!copies.isEmpty()) {
            questionnaireQuestionRepository.saveAll(copies);
        }
    }

    private void replaceQuestions(QuestionnaireVersion version, List<QuestionnaireQuestionItemRequest> items) {
        assertDraftEditable(version);
        UUID tenantId = SecurityUtils.requireTenantId();
        questionnaireQuestionRepository.deleteByQuestionnaireVersionId(version.getId());

        if (items == null || items.isEmpty()) {
            return;
        }

        Set<UUID> seen = new HashSet<>();
        List<QuestionnaireQuestion> toSave = new ArrayList<>();
        for (QuestionnaireQuestionItemRequest item : items) {
            if (!seen.add(item.questionId())) {
                throw new BusinessException("Duplicate questionId in request: " + item.questionId(), "VALIDATION_ERROR");
            }
            Question question = questionRepository.findByIdAndTenantIdAndIsDeletedFalse(item.questionId(), tenantId)
                    .orElseThrow(() -> new BusinessException(
                            "Question not found or deleted: " + item.questionId(),
                            "VALIDATION_ERROR"
                    ));
            if (question.getReviewStatus() != ReviewStatus.APPROVED || !question.isActive()) {
                throw new BusinessException(
                        "Only APPROVED and active questions can be attached: " + question.getQuestionCode(),
                        "VALIDATION_ERROR"
                );
            }

            QuestionnaireQuestion qq = new QuestionnaireQuestion();
            qq.setQuestionnaireVersion(version);
            qq.setQuestion(question);
            qq.setDisplayOrder(item.displayOrder());
            qq.setMandatory(item.isMandatory() == null || item.isMandatory());
            qq.setSectionCode(item.sectionCode());
            qq.setSectionName(item.sectionName());
            qq.setConditionConfig(item.conditionConfig());
            toSave.add(qq);
        }
        questionnaireQuestionRepository.saveAll(toSave);
    }

    private void assertDraftEditable(QuestionnaireVersion version) {
        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException(
                    "Structural edits are not allowed on PUBLISHED or ARCHIVED questionnaire versions",
                    version.getStatus() == VersionStatus.PUBLISHED ? "QUESTIONNAIRE_PUBLISHED" : "INVALID_STATE"
            );
        }
    }

    private Questionnaire requireQuestionnaire(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        return questionnaireRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
    }

    private QuestionnaireVersion requireVersion(UUID versionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        QuestionnaireVersion version = questionnaireVersionRepository
                .findByIdAndQuestionnaireTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire version not found"));
        if (version.getQuestionnaire().isDeleted()) {
            throw new ResourceNotFoundException("Questionnaire version not found");
        }
        return version;
    }

    private QuestionnaireVersion requireLatestVersion(Questionnaire questionnaire) {
        return questionnaireVersionRepository
                .findTopByQuestionnaireIdOrderByVersionNumberDesc(questionnaire.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire version not found"));
    }

    private QuestionnaireVersion resolveVersion(Questionnaire questionnaire, UUID versionId) {
        if (versionId != null) {
            QuestionnaireVersion version = requireVersion(versionId);
            if (!version.getQuestionnaire().getId().equals(questionnaire.getId())) {
                throw new ResourceNotFoundException("Questionnaire version not found");
            }
            return version;
        }
        if (questionnaire.getCurrentVersionId() != null) {
            return questionnaireVersionRepository.findById(questionnaire.getCurrentVersionId())
                    .orElseGet(() -> requireLatestVersion(questionnaire));
        }
        return requireLatestVersion(questionnaire);
    }

    private QuestionnaireListItemResponse toListItem(Questionnaire questionnaire) {
        return new QuestionnaireListItemResponse(
                questionnaire.getId(),
                questionnaire.getAssessmentType(),
                questionnaire.getCode(),
                questionnaire.getName(),
                questionnaire.getDescription(),
                questionnaire.getStatus(),
                questionnaire.getCurrentVersionId(),
                questionnaire.isShuffleQuestions(),
                questionnaire.isActive()
        );
    }

    private QuestionnaireResponse toResponse(
            Questionnaire questionnaire,
            QuestionnaireVersion version,
            List<QuestionnaireQuestion> questions
    ) {
        List<QuestionnaireQuestionSummaryResponse> questionResponses = questions.stream()
                .map(qq -> new QuestionnaireQuestionSummaryResponse(
                        qq.getId(),
                        qq.getQuestion().getId(),
                        qq.getQuestion().getQuestionCode(),
                        qq.getQuestion().getDefaultText(),
                        qq.getDisplayOrder(),
                        qq.isMandatory(),
                        qq.getSectionCode(),
                        qq.getSectionName(),
                        qq.getConditionConfig()
                ))
                .toList();

        return new QuestionnaireResponse(
                questionnaire.getId(),
                questionnaire.getTenant().getId(),
                questionnaire.getAssessmentType(),
                questionnaire.getCode(),
                questionnaire.getName(),
                questionnaire.getDescription(),
                questionnaire.getStatus(),
                questionnaire.getCurrentVersionId(),
                questionnaire.isShuffleQuestions(),
                questionnaire.isActive(),
                version.getId(),
                version.getVersionNumber(),
                version.getStatus(),
                questionResponses
        );
    }
}
