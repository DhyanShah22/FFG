package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.Language;
import com.antarang.cap.domain.entity.OptionTranslation;
import com.antarang.cap.domain.entity.Question;
import com.antarang.cap.domain.entity.QuestionCategory;
import com.antarang.cap.domain.entity.QuestionOption;
import com.antarang.cap.domain.entity.QuestionRule;
import com.antarang.cap.domain.entity.QuestionTranslation;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.QuestionType;
import com.antarang.cap.domain.enums.QuestionTypeMapper;
import com.antarang.cap.domain.enums.ReviewStatus;
import com.antarang.cap.domain.enums.VersionStatus;
import com.antarang.cap.dto.request.CreateQuestionOptionRequest;
import com.antarang.cap.dto.request.CreateQuestionRequest;
import com.antarang.cap.dto.request.CreateQuestionRuleRequest;
import com.antarang.cap.dto.request.UpdateQuestionOptionRequest;
import com.antarang.cap.dto.request.UpdateQuestionRequest;
import com.antarang.cap.dto.request.UpdateQuestionStatusRequest;
import com.antarang.cap.dto.request.UpsertQuestionTranslationsRequest;
import com.antarang.cap.dto.response.OptionTranslationResponse;
import com.antarang.cap.dto.response.QuestionDetailResponse;
import com.antarang.cap.dto.response.QuestionOptionResponse;
import com.antarang.cap.dto.response.QuestionRuleResponse;
import com.antarang.cap.dto.response.QuestionSummaryResponse;
import com.antarang.cap.dto.response.QuestionTranslationResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.LanguageRepository;
import com.antarang.cap.repository.OptionTranslationRepository;
import com.antarang.cap.repository.QuestionCategoryRepository;
import com.antarang.cap.repository.QuestionOptionRepository;
import com.antarang.cap.repository.QuestionRepository;
import com.antarang.cap.repository.QuestionRuleRepository;
import com.antarang.cap.repository.QuestionTranslationRepository;
import com.antarang.cap.repository.QuestionnaireQuestionRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionCategoryRepository questionCategoryRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionTranslationRepository questionTranslationRepository;
    private final OptionTranslationRepository optionTranslationRepository;
    private final QuestionRuleRepository questionRuleRepository;
    private final QuestionnaireQuestionRepository questionnaireQuestionRepository;
    private final LanguageRepository languageRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;

    public QuestionService(
            QuestionRepository questionRepository,
            QuestionCategoryRepository questionCategoryRepository,
            QuestionOptionRepository questionOptionRepository,
            QuestionTranslationRepository questionTranslationRepository,
            OptionTranslationRepository optionTranslationRepository,
            QuestionRuleRepository questionRuleRepository,
            QuestionnaireQuestionRepository questionnaireQuestionRepository,
            LanguageRepository languageRepository,
            TenantRepository tenantRepository,
            AuditService auditService
    ) {
        this.questionRepository = questionRepository;
        this.questionCategoryRepository = questionCategoryRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.questionTranslationRepository = questionTranslationRepository;
        this.optionTranslationRepository = optionTranslationRepository;
        this.questionRuleRepository = questionRuleRepository;
        this.questionnaireQuestionRepository = questionnaireQuestionRepository;
        this.languageRepository = languageRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    @Transactional
    public QuestionDetailResponse create(CreateQuestionRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Tenant tenant = loadTenant(tenantId);
        QuestionCategory category = loadCategory(request.categoryId(), tenantId);
        QuestionType questionType = QuestionTypeMapper.fromApi(request.questionType());

        List<CreateQuestionOptionRequest> options = request.options() != null ? request.options() : List.of();
        validateOptionsForType(questionType, options.size());

        if (questionRepository.existsByTenantIdAndQuestionCodeAndIsDeletedFalse(tenantId, request.questionCode())) {
            throw new BusinessException("Question code already exists", "DUPLICATE_RESOURCE");
        }

        Question question = new Question();
        question.setTenant(tenant);
        question.setAssessmentType(category.getAssessmentType());
        question.setCategory(category);
        question.setQuestionCode(request.questionCode().trim());
        question.setQuestionType(questionType);
        question.setDefaultText(request.defaultText().trim());
        question.setHelpText(request.helpText());
        question.setDifficultyLevel(request.difficultyLevel());
        question.setReviewStatus(request.reviewStatus() != null ? request.reviewStatus() : ReviewStatus.DRAFT);
        question.setRequiredDefault(request.isRequiredDefault() == null || request.isRequiredDefault());
        question.setScoringEnabled(request.scoringEnabled() == null || request.scoringEnabled());
        question.setMetadata(request.metadata());

        Question saved = questionRepository.save(question);

        for (CreateQuestionOptionRequest optionRequest : options) {
            createOptionEntity(saved, optionRequest);
        }

        auditService.record("Question", saved.getId(), AuditAction.CREATE, null, Map.of(
                "questionCode", saved.getQuestionCode(),
                "questionType", saved.getQuestionType().name()
        ));

        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionSummaryResponse> list(
            AssessmentType assessmentType,
            UUID categoryId,
            String questionType,
            ReviewStatus reviewStatus,
            UUID languageId,
            String search,
            int page,
            int size
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();

        ReviewStatus effectiveReviewStatus = reviewStatus;
        Boolean effectiveIsActive = null;
        if (reviewStatus == null) {
            effectiveReviewStatus = ReviewStatus.APPROVED;
            effectiveIsActive = true;
        }

        QuestionType parsedType = questionType != null && !questionType.isBlank()
                ? QuestionTypeMapper.fromApi(questionType)
                : null;
        String searchTerm = search != null && !search.isBlank() ? search.trim() : null;
        Pageable pageable = PageRequest.of(page, size);

        Page<Question> result;
        if (searchTerm == null) {
            result = questionRepository.search(
                    tenantId,
                    assessmentType,
                    categoryId,
                    parsedType,
                    effectiveReviewStatus,
                    effectiveIsActive,
                    pageable
            );
        } else {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            result = questionRepository.searchWithText(
                    tenantId,
                    assessmentType,
                    categoryId,
                    parsedType,
                    effectiveReviewStatus,
                    effectiveIsActive,
                    searchPattern,
                    pageable
            );
        }

        Map<UUID, String> localization = new HashMap<>();
        if (languageId != null) {
            for (Question question : result.getContent()) {
                questionTranslationRepository.findByQuestionIdAndLanguageId(question.getId(), languageId)
                        .ifPresent(translation -> localization.put(question.getId(), translation.getQuestionText()));
            }
        }

        return PageResponse.from(result.map(question -> toSummaryResponse(
                question,
                localization.get(question.getId())
        )));
    }

    @Transactional(readOnly = true)
    public QuestionDetailResponse getById(UUID questionId) {
        return toDetailResponse(loadQuestion(questionId));
    }

    @Transactional
    public QuestionDetailResponse update(UUID questionId, UpdateQuestionRequest request) {
        Question question = loadQuestion(questionId);
        UUID tenantId = SecurityUtils.requireTenantId();
        boolean onPublished = isOnPublishedVersion(question.getId());

        Map<String, Object> oldValue = Map.of(
                "defaultText", question.getDefaultText(),
                "reviewStatus", question.getReviewStatus().name()
        );

        if (request.categoryId() != null) {
            QuestionCategory category = loadCategory(request.categoryId(), tenantId);
            question.setCategory(category);
            question.setAssessmentType(category.getAssessmentType());
        }
        if (request.questionType() != null && !request.questionType().isBlank()) {
            question.setQuestionType(QuestionTypeMapper.fromApi(request.questionType()));
        }
        if (request.defaultText() != null && !request.defaultText().isBlank()) {
            question.setDefaultText(request.defaultText().trim());
        }
        if (request.helpText() != null) {
            question.setHelpText(request.helpText());
        }
        if (request.difficultyLevel() != null) {
            question.setDifficultyLevel(request.difficultyLevel());
        }
        if (request.reviewStatus() != null) {
            question.setReviewStatus(request.reviewStatus());
        }
        if (request.isRequiredDefault() != null) {
            question.setRequiredDefault(request.isRequiredDefault());
        }
        if (request.scoringEnabled() != null) {
            question.setScoringEnabled(request.scoringEnabled());
        }
        if (request.isActive() != null) {
            question.setActive(request.isActive());
        }
        if (request.metadata() != null) {
            question.setMetadata(request.metadata());
        }

        if (request.options() != null) {
            syncOptions(question, request.options(), onPublished);
        }

        if (question.getQuestionType() == QuestionType.RADIO) {
            long activeOptions = questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId()).stream()
                    .filter(QuestionOption::isActive)
                    .count();
            if (activeOptions < 2) {
                throw new BusinessException("RADIO questions require at least 2 options", "VALIDATION_ERROR");
            }
        }

        Question saved = questionRepository.save(question);
        auditService.record("Question", saved.getId(), AuditAction.UPDATE, oldValue, Map.of(
                "defaultText", saved.getDefaultText(),
                "reviewStatus", saved.getReviewStatus().name()
        ));
        return toDetailResponse(saved);
    }

    @Transactional
    public void softDelete(UUID questionId) {
        Question question = loadQuestion(questionId);
        if (isOnPublishedVersion(question.getId())) {
            throw new BusinessException("Question is used on a published questionnaire version", "QUESTION_IN_USE");
        }
        question.setDeleted(true);
        question.setActive(false);
        questionRepository.save(question);
        auditService.record("Question", question.getId(), AuditAction.DELETE, Map.of("isDeleted", false), Map.of("isDeleted", true));
    }

    @Transactional
    public QuestionOptionResponse addOption(UUID questionId, CreateQuestionOptionRequest request) {
        Question question = loadQuestion(questionId);
        if (questionOptionRepository.existsByQuestionIdAndOptionCode(question.getId(), request.optionCode())) {
            throw new BusinessException("Option code already exists for this question", "DUPLICATE_RESOURCE");
        }
        QuestionOption option = createOptionEntity(question, request);
        return toOptionResponse(option, List.of());
    }

    @Transactional
    public QuestionDetailResponse upsertTranslations(UUID questionId, UpsertQuestionTranslationsRequest request) {
        Question question = loadQuestion(questionId);

        if (request.translations() != null) {
            for (UpsertQuestionTranslationsRequest.QuestionTranslationItem item : request.translations()) {
                Language language = loadLanguage(item.languageId());
                QuestionTranslation translation = questionTranslationRepository
                        .findByQuestionIdAndLanguageId(question.getId(), language.getId())
                        .orElseGet(QuestionTranslation::new);
                translation.setQuestion(question);
                translation.setLanguage(language);
                translation.setQuestionText(item.questionText().trim());
                translation.setHelpText(item.helpText());
                questionTranslationRepository.save(translation);
            }
        }

        if (request.optionTranslations() != null) {
            for (UpsertQuestionTranslationsRequest.OptionTranslationItem item : request.optionTranslations()) {
                QuestionOption option = questionOptionRepository.findByIdAndQuestionId(item.optionId(), question.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Question option not found"));
                Language language = loadLanguage(item.languageId());
                OptionTranslation translation = optionTranslationRepository
                        .findByOptionIdAndLanguageId(option.getId(), language.getId())
                        .orElseGet(OptionTranslation::new);
                translation.setOption(option);
                translation.setLanguage(language);
                translation.setOptionText(item.optionText().trim());
                optionTranslationRepository.save(translation);
            }
        }

        return toDetailResponse(question);
    }

    @Transactional
    public QuestionRuleResponse addRule(UUID questionId, CreateQuestionRuleRequest request) {
        Question question = loadQuestion(questionId);
        QuestionRule rule = new QuestionRule();
        rule.setQuestion(question);
        rule.setRuleType(request.ruleType());
        rule.setRuleConfig(request.ruleConfig());
        return toRuleResponse(questionRuleRepository.save(rule));
    }

    @Transactional(readOnly = true)
    public List<QuestionRuleResponse> listRules(UUID questionId) {
        loadQuestion(questionId);
        return questionRuleRepository.findByQuestionIdAndIsActiveTrue(questionId).stream()
                .map(this::toRuleResponse)
                .toList();
    }

    @Transactional
    public void deactivateRule(UUID questionId, UUID ruleId) {
        loadQuestion(questionId);
        QuestionRule rule = questionRuleRepository.findByIdAndQuestionId(ruleId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question rule not found"));
        rule.setActive(false);
        questionRuleRepository.save(rule);
    }

    @Transactional
    public QuestionDetailResponse updateStatus(UUID questionId, UpdateQuestionStatusRequest request) {
        Question question = loadQuestion(questionId);
        Map<String, Object> oldValue = Map.of(
                "reviewStatus", question.getReviewStatus().name(),
                "isActive", question.isActive()
        );

        if (request.reviewStatus() != null) {
            question.setReviewStatus(request.reviewStatus());
        }
        if (request.isActive() != null) {
            question.setActive(request.isActive());
        }

        Question saved = questionRepository.save(question);
        auditService.record("Question", saved.getId(), AuditAction.STATUS_CHANGE, oldValue, Map.of(
                "reviewStatus", saved.getReviewStatus().name(),
                "isActive", saved.isActive()
        ));
        return toDetailResponse(saved);
    }

    private void syncOptions(Question question, List<UpdateQuestionOptionRequest> optionRequests, boolean onPublished) {
        List<QuestionOption> existing = questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId());
        Map<UUID, QuestionOption> existingById = existing.stream()
                .collect(Collectors.toMap(QuestionOption::getId, option -> option));

        Set<UUID> retainedIds = new HashSet<>();
        for (UpdateQuestionOptionRequest optionRequest : optionRequests) {
            if (optionRequest.id() != null) {
                QuestionOption option = existingById.get(optionRequest.id());
                if (option == null) {
                    throw new ResourceNotFoundException("Question option not found");
                }
                option.setOptionCode(optionRequest.optionCode().trim());
                option.setDefaultText(optionRequest.defaultText().trim());
                if (optionRequest.scoreValue() != null) {
                    option.setScoreValue(optionRequest.scoreValue());
                }
                if (optionRequest.displayOrder() != null) {
                    option.setDisplayOrder(optionRequest.displayOrder());
                }
                if (optionRequest.metadata() != null) {
                    option.setMetadata(optionRequest.metadata());
                }
                if (optionRequest.isActive() != null) {
                    if (onPublished && !optionRequest.isActive() && option.isActive()) {
                        throw new BusinessException(
                                "Cannot remove options from a question on a published questionnaire version",
                                "QUESTION_IN_PUBLISHED_VERSION"
                        );
                    }
                    option.setActive(optionRequest.isActive());
                }
                questionOptionRepository.save(option);
                retainedIds.add(option.getId());
            } else {
                if (questionOptionRepository.existsByQuestionIdAndOptionCode(question.getId(), optionRequest.optionCode())) {
                    throw new BusinessException("Option code already exists for this question", "DUPLICATE_RESOURCE");
                }
                QuestionOption option = new QuestionOption();
                option.setQuestion(question);
                option.setOptionCode(optionRequest.optionCode().trim());
                option.setDefaultText(optionRequest.defaultText().trim());
                option.setScoreValue(optionRequest.scoreValue());
                option.setDisplayOrder(optionRequest.displayOrder() != null ? optionRequest.displayOrder() : 0);
                option.setMetadata(optionRequest.metadata());
                if (optionRequest.isActive() != null) {
                    option.setActive(optionRequest.isActive());
                }
                QuestionOption saved = questionOptionRepository.save(option);
                retainedIds.add(saved.getId());
            }
        }

        for (QuestionOption option : existing) {
            if (!retainedIds.contains(option.getId())) {
                if (onPublished) {
                    throw new BusinessException(
                            "Cannot remove options from a question on a published questionnaire version",
                            "QUESTION_IN_PUBLISHED_VERSION"
                    );
                }
                List<OptionTranslation> translations = optionTranslationRepository.findByOptionId(option.getId());
                optionTranslationRepository.deleteAll(translations);
                questionOptionRepository.delete(option);
            }
        }
    }

    private QuestionOption createOptionEntity(Question question, CreateQuestionOptionRequest request) {
        QuestionOption option = new QuestionOption();
        option.setQuestion(question);
        option.setOptionCode(request.optionCode().trim());
        option.setDefaultText(request.defaultText().trim());
        option.setScoreValue(request.scoreValue());
        option.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        option.setMetadata(request.metadata());
        return questionOptionRepository.save(option);
    }

    private void validateOptionsForType(QuestionType questionType, int optionCount) {
        if (questionType == QuestionType.RADIO && optionCount < 2) {
            throw new BusinessException("RADIO questions require at least 2 options", "VALIDATION_ERROR");
        }
    }

    private boolean isOnPublishedVersion(UUID questionId) {
        return questionnaireQuestionRepository.isQuestionOnVersionWithStatus(questionId, VersionStatus.PUBLISHED);
    }

    private Question loadQuestion(UUID questionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        return questionRepository.findByIdAndTenantIdAndIsDeletedFalse(questionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    }

    private QuestionCategory loadCategory(UUID categoryId, UUID tenantId) {
        return questionCategoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Question category not found"));
    }

    private Language loadLanguage(UUID languageId) {
        return languageRepository.findById(languageId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));
    }

    private Tenant loadTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
    }

    private QuestionSummaryResponse toSummaryResponse(Question question, String localizedText) {
        return new QuestionSummaryResponse(
                question.getId(),
                question.getTenant().getId(),
                question.getAssessmentType(),
                question.getCategory().getId(),
                question.getCategory().getCode(),
                question.getQuestionCode(),
                question.getQuestionType(),
                question.getDefaultText(),
                question.getHelpText(),
                localizedText,
                question.getDifficultyLevel(),
                question.getReviewStatus(),
                question.isRequiredDefault(),
                question.isScoringEnabled(),
                question.isActive(),
                question.getMetadata()
        );
    }

    private QuestionDetailResponse toDetailResponse(Question question) {
        List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId());
        List<QuestionTranslation> translations = questionTranslationRepository.findByQuestionId(question.getId());
        List<OptionTranslation> optionTranslations = optionTranslationRepository.findByOptionQuestionId(question.getId());
        Map<UUID, List<OptionTranslation>> optionTranslationsByOptionId = optionTranslations.stream()
                .collect(Collectors.groupingBy(ot -> ot.getOption().getId()));

        List<QuestionOptionResponse> optionResponses = options.stream()
                .map(option -> toOptionResponse(option, optionTranslationsByOptionId.getOrDefault(option.getId(), List.of())))
                .toList();

        List<QuestionTranslationResponse> translationResponses = translations.stream()
                .map(this::toQuestionTranslationResponse)
                .toList();

        List<QuestionRuleResponse> rules = questionRuleRepository.findByQuestionIdAndIsActiveTrue(question.getId()).stream()
                .map(this::toRuleResponse)
                .toList();

        return new QuestionDetailResponse(
                question.getId(),
                question.getTenant().getId(),
                question.getAssessmentType(),
                question.getCategory().getId(),
                question.getCategory().getCode(),
                question.getCategory().getName(),
                question.getQuestionCode(),
                question.getQuestionType(),
                question.getDefaultText(),
                question.getHelpText(),
                question.getDifficultyLevel(),
                question.getReviewStatus(),
                question.isRequiredDefault(),
                question.isScoringEnabled(),
                question.isActive(),
                question.getMetadata(),
                optionResponses,
                translationResponses,
                rules
        );
    }

    private QuestionOptionResponse toOptionResponse(QuestionOption option, List<OptionTranslation> translations) {
        List<OptionTranslationResponse> translationResponses = translations.stream()
                .map(this::toOptionTranslationResponse)
                .toList();
        return new QuestionOptionResponse(
                option.getId(),
                option.getOptionCode(),
                option.getDefaultText(),
                option.getScoreValue(),
                option.getDisplayOrder(),
                option.getMetadata(),
                option.isActive(),
                translationResponses
        );
    }

    private QuestionTranslationResponse toQuestionTranslationResponse(QuestionTranslation translation) {
        return new QuestionTranslationResponse(
                translation.getId(),
                translation.getLanguage().getId(),
                translation.getLanguage().getCode(),
                translation.getQuestionText(),
                translation.getHelpText()
        );
    }

    private OptionTranslationResponse toOptionTranslationResponse(OptionTranslation translation) {
        return new OptionTranslationResponse(
                translation.getId(),
                translation.getOption().getId(),
                translation.getLanguage().getId(),
                translation.getLanguage().getCode(),
                translation.getOptionText()
        );
    }

    private QuestionRuleResponse toRuleResponse(QuestionRule rule) {
        return new QuestionRuleResponse(
                rule.getId(),
                rule.getQuestion().getId(),
                rule.getRuleType(),
                rule.getRuleConfig(),
                rule.isActive(),
                rule.getCreatedAt()
        );
    }
}
