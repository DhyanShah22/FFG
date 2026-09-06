package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.Assessment;
import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.AssessmentConfiguration;
import com.antarang.cap.domain.entity.AssessmentConfigurationGroupItem;
import com.antarang.cap.domain.entity.AssessmentConfigurationGroupOutput;
import com.antarang.cap.domain.entity.AssessmentResponse;
import com.antarang.cap.domain.entity.AssessmentResponseOption;
import com.antarang.cap.domain.entity.IarLogicVersion;
import com.antarang.cap.domain.entity.Language;
import com.antarang.cap.domain.entity.OptionTranslation;
import com.antarang.cap.domain.entity.Question;
import com.antarang.cap.domain.entity.QuestionOption;
import com.antarang.cap.domain.entity.QuestionTranslation;
import com.antarang.cap.domain.entity.QuestionnaireQuestion;
import com.antarang.cap.domain.entity.ReportTemplateVersion;
import com.antarang.cap.domain.entity.ScoringRuleVersion;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.CompletionRule;
import com.antarang.cap.domain.enums.ScoreStatus;
import com.antarang.cap.domain.enums.SubmissionReason;
import com.antarang.cap.domain.enums.TimerMode;
import com.antarang.cap.dto.request.StartAssessmentRequest;
import com.antarang.cap.dto.request.SubmitAttemptRequest;
import com.antarang.cap.dto.request.UpsertAttemptResponsesRequest;
import com.antarang.cap.dto.request.UpsertQuestionTimingRequest;
import com.antarang.cap.dto.response.AttemptDetailResponse;
import com.antarang.cap.dto.response.AttemptHistoryItemResponse;
import com.antarang.cap.dto.response.AttemptQuestionOptionResponse;
import com.antarang.cap.dto.response.AttemptQuestionResponse;
import com.antarang.cap.dto.response.AttemptTimerResponse;
import com.antarang.cap.dto.response.DomainScoreResponse;
import com.antarang.cap.dto.response.StartAssessmentResponse;
import com.antarang.cap.dto.response.StudentAssessmentListItemResponse;
import com.antarang.cap.dto.response.SubmitAttemptResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import com.antarang.cap.repository.AssessmentConfigurationGroupOutputRepository;
import com.antarang.cap.repository.AssessmentConfigurationRepository;
import com.antarang.cap.repository.AssessmentResponseOptionRepository;
import com.antarang.cap.repository.AssessmentResponseRepository;
import com.antarang.cap.repository.IarLogicVersionRepository;
import com.antarang.cap.repository.LanguageRepository;
import com.antarang.cap.repository.OptionTranslationRepository;
import com.antarang.cap.repository.QuestionOptionRepository;
import com.antarang.cap.repository.QuestionTranslationRepository;
import com.antarang.cap.repository.QuestionnaireQuestionRepository;
import com.antarang.cap.repository.ReportTemplateVersionRepository;
import com.antarang.cap.repository.ScoringRuleVersionRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.StudentAccessGuard;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssessmentAttemptService {

    private final ConfigurationResolutionService configurationResolutionService;
    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentConfigurationRepository configurationRepository;
    private final AssessmentConfigurationGroupOutputRepository outputRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final LanguageRepository languageRepository;
    private final IarLogicVersionRepository iarLogicVersionRepository;
    private final ScoringRuleVersionRepository scoringRuleVersionRepository;
    private final ReportTemplateVersionRepository reportTemplateVersionRepository;
    private final QuestionnaireQuestionRepository questionnaireQuestionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionTranslationRepository questionTranslationRepository;
    private final OptionTranslationRepository optionTranslationRepository;
    private final AssessmentResponseRepository responseRepository;
    private final AssessmentResponseOptionRepository responseOptionRepository;
    private final AttemptExpiryGuard attemptExpiryGuard;
    private final AttemptStatusHistoryService statusHistoryService;
    private final ResponseService responseService;
    private final QuestionTimingService questionTimingService;
    private final QuestionRuleEvaluator questionRuleEvaluator;
    private final ScoringService scoringService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final StudentAccessGuard studentAccessGuard;

    public AssessmentAttemptService(
            ConfigurationResolutionService configurationResolutionService,
            AssessmentAttemptRepository attemptRepository,
            AssessmentConfigurationRepository configurationRepository,
            AssessmentConfigurationGroupOutputRepository outputRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository,
            LanguageRepository languageRepository,
            IarLogicVersionRepository iarLogicVersionRepository,
            ScoringRuleVersionRepository scoringRuleVersionRepository,
            ReportTemplateVersionRepository reportTemplateVersionRepository,
            QuestionnaireQuestionRepository questionnaireQuestionRepository,
            QuestionOptionRepository questionOptionRepository,
            QuestionTranslationRepository questionTranslationRepository,
            OptionTranslationRepository optionTranslationRepository,
            AssessmentResponseRepository responseRepository,
            AssessmentResponseOptionRepository responseOptionRepository,
            AttemptExpiryGuard attemptExpiryGuard,
            AttemptStatusHistoryService statusHistoryService,
            ResponseService responseService,
            QuestionTimingService questionTimingService,
            QuestionRuleEvaluator questionRuleEvaluator,
            ScoringService scoringService,
            ActivityLogService activityLogService,
            NotificationService notificationService,
            StudentAccessGuard studentAccessGuard
    ) {
        this.configurationResolutionService = configurationResolutionService;
        this.attemptRepository = attemptRepository;
        this.configurationRepository = configurationRepository;
        this.outputRepository = outputRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.languageRepository = languageRepository;
        this.iarLogicVersionRepository = iarLogicVersionRepository;
        this.scoringRuleVersionRepository = scoringRuleVersionRepository;
        this.reportTemplateVersionRepository = reportTemplateVersionRepository;
        this.questionnaireQuestionRepository = questionnaireQuestionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.questionTranslationRepository = questionTranslationRepository;
        this.optionTranslationRepository = optionTranslationRepository;
        this.responseRepository = responseRepository;
        this.responseOptionRepository = responseOptionRepository;
        this.attemptExpiryGuard = attemptExpiryGuard;
        this.statusHistoryService = statusHistoryService;
        this.responseService = responseService;
        this.questionTimingService = questionTimingService;
        this.questionRuleEvaluator = questionRuleEvaluator;
        this.scoringService = scoringService;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
        this.studentAccessGuard = studentAccessGuard;
    }

    @Transactional(readOnly = true)
    public List<StudentAssessmentListItemResponse> listStudentAssessments(UUID studentId) {
        studentAccessGuard.assertStudentAccess(studentId);
        try {
            ConfigurationResolutionService.ResolutionResult resolution =
                    configurationResolutionService.resolve(studentId, null);
            List<StudentAssessmentListItemResponse> items = new ArrayList<>();
            for (AssessmentConfigurationGroupItem groupItem : resolution.items()) {
                items.add(buildStudentAssessmentItem(studentId, resolution.group().getId(), groupItem));
            }
            return items;
        } catch (ResourceNotFoundException ex) {
            return List.of();
        }
    }

    @Transactional
    public StartAssessmentResponse start(UUID assessmentId, StartAssessmentRequest request) {
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        UUID studentId = request != null && request.studentId() != null
                ? request.studentId()
                : principal.getId();
        studentAccessGuard.assertStudentAccess(studentId);

        ConfigurationResolutionService.ResolutionResult resolution =
                configurationResolutionService.resolve(studentId, assessmentId);
        AssessmentConfigurationGroupItem groupItem = resolution.matchedItem();
        if (groupItem == null) {
            throw new BusinessException("No configuration found for assessment", "CONFIGURATION_NOT_FOUND");
        }

        AssessmentConfiguration execConfig = configurationRepository
                .findByAssessmentVersionId(groupItem.getAssessmentVersion().getId())
                .orElseThrow(() -> new BusinessException("Assessment execution config not found", "CONFIGURATION_NOT_FOUND"));

        List<AssessmentAttempt> inProgress = attemptRepository
                .findByStudentIdAndAssessmentIdAndStatus(studentId, assessmentId, AttemptStatus.IN_PROGRESS);
        if (!inProgress.isEmpty()) {
            AssessmentAttempt existing = inProgress.getFirst();
            if (execConfig.isAllowResume()) {
                attemptExpiryGuard.assertWritable(existing);
                return buildStartResponse(existing, execConfig, true);
            }
            closeAttemptForRestart(existing);
        }

        validateAttemptPolicy(studentId, assessmentId, execConfig);

        UUID languageOverride = request != null ? request.assessmentLanguageId() : null;
        AssessmentAttempt attempt = createAttempt(
                studentId,
                groupItem,
                execConfig,
                languageOverride,
                request,
                principal
        );
        return buildStartResponse(attempt, execConfig, false);
    }

    @Transactional
    public AttemptDetailResponse getAttempt(UUID attemptId) {
        AssessmentAttempt attempt = requireAttempt(attemptId);
        studentAccessGuard.assertAttemptOwner(attempt);
        attemptExpiryGuard.assertWritable(attempt);
        AssessmentConfiguration execConfig = configurationRepository
                .findByAssessmentVersionId(attempt.getAssessmentVersion().getId())
                .orElse(null);
        return buildAttemptDetail(attempt, execConfig);
    }

    @Transactional
    public void saveResponses(UUID attemptId, UpsertAttemptResponsesRequest request) {
        responseService.upsertResponses(attemptId, request);
    }

    @Transactional
    public void saveTimings(UUID attemptId, UpsertQuestionTimingRequest request) {
        questionTimingService.upsertTimings(attemptId, request);
    }

    @Transactional
    public SubmitAttemptResponse submit(UUID attemptId, SubmitAttemptRequest request) {
        AssessmentAttempt attempt = requireAttempt(attemptId);
        studentAccessGuard.assertAttemptOwner(attempt);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BusinessException("Attempt is not in progress", "ATTEMPT_NOT_IN_PROGRESS");
        }

        attemptExpiryGuard.assertWritable(attempt);

        validateMandatoryResponses(attempt);

        SubmissionReason submissionReason = attemptExpiryGuard.resolveSubmissionReason(
                attempt,
                request != null ? request.submissionReason() : null
        );

        AttemptStatus oldStatus = attempt.getStatus();
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(Instant.now());
        attempt.setSubmissionReason(submissionReason);
        if (request != null && request.clientElapsedSeconds() != null) {
            attempt.setElapsedSeconds(request.clientElapsedSeconds());
        } else if (attempt.getStartedAt() != null) {
            attempt.setElapsedSeconds(attemptExpiryGuard.elapsedSeconds(attempt));
        }
        attempt.setScoreStatus(ScoreStatus.PENDING);
        statusHistoryService.record(attempt, oldStatus, AttemptStatus.SUBMITTED, "Submitted");
        attemptRepository.save(attempt);

        List<DomainScoreResponse> domainScores;
        try {
            domainScores = scoringService.scoreOnSubmit(attempt);
        } catch (BusinessException ex) {
            attempt.setScoreStatus(ScoreStatus.FAILED);
            attemptRepository.save(attempt);
            throw ex;
        }
        attemptRepository.save(attempt);
        activityLogService.logAssessmentSubmit(attempt);

        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", attempt.getStudent().getFirstName() != null ? attempt.getStudent().getFirstName() : "Student");
        variables.put("assessmentCode", attempt.getAssessment().getCode().name());
        notificationService.enqueue(attempt.getStudent().getId(), "ASSESSMENT_COMPLETED", variables);

        return new SubmitAttemptResponse(
                attempt.getId(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                attempt.getElapsedSeconds(),
                attempt.getSubmissionReason(),
                attempt.getScoreStatus(),
                domainScores
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AttemptHistoryItemResponse> attemptHistory(UUID studentId, UUID assessmentId, int page, int size) {
        studentAccessGuard.assertStudentAccess(studentId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
        Page<AssessmentAttempt> result = assessmentId != null
                ? attemptRepository.findByStudentIdAndAssessmentIdOrderByStartedAtDesc(studentId, assessmentId, pageable)
                : attemptRepository.findByStudentIdOrderByStartedAtDesc(studentId, pageable);

        Map<UUID, Integer> attemptNumbers = computeAttemptNumbers(studentId, result.getContent());
        return PageResponse.from(result.map(attempt -> toHistoryItem(attempt, attemptNumbers.getOrDefault(attempt.getId(), 1))));
    }

    private StudentAssessmentListItemResponse buildStudentAssessmentItem(
            UUID studentId,
            UUID configurationGroupId,
            AssessmentConfigurationGroupItem groupItem
    ) {
        Assessment assessment = groupItem.getAssessment();
        AssessmentConfiguration execConfig = configurationRepository
                .findByAssessmentVersionId(groupItem.getAssessmentVersion().getId())
                .orElse(null);

        long attemptsUsed = countCompletedAttempts(studentId, assessment.getId());
        List<AssessmentAttempt> active = attemptRepository
                .findByStudentIdAndAssessmentIdAndStatus(studentId, assessment.getId(), AttemptStatus.IN_PROGRESS);
        UUID activeAttemptId = active.isEmpty() ? null : active.getFirst().getId();

        String status = "NOT_STARTED";
        if (activeAttemptId != null) {
            status = "IN_PROGRESS";
        } else if (attemptsUsed > 0) {
            status = "SUBMITTED";
        }

        return new StudentAssessmentListItemResponse(
                assessment.getId(),
                assessment.getCode().name(),
                assessment.getName(),
                configurationGroupId,
                status,
                execConfig != null ? execConfig.getTimerMode() : TimerMode.COUNT_UP,
                execConfig != null && execConfig.isAllowResume(),
                execConfig != null ? execConfig.getMaxAttempts() : 1,
                attemptsUsed,
                activeAttemptId
        );
    }

    private long countCompletedAttempts(UUID studentId, UUID assessmentId) {
        return attemptRepository.countByStudentIdAndAssessmentIdAndStatusIn(
                studentId,
                assessmentId,
                List.of(AttemptStatus.SUBMITTED, AttemptStatus.EVALUATED)
        );
    }

    private void closeAttemptForRestart(AssessmentAttempt attempt) {
        AttemptStatus oldStatus = attempt.getStatus();
        attempt.setStatus(AttemptStatus.CANCELLED);
        attempt.setSubmissionReason(SubmissionReason.RESTARTED);
        attempt.setSubmittedAt(Instant.now());
        statusHistoryService.record(attempt, oldStatus, AttemptStatus.CANCELLED, "Restarted");
        attemptRepository.save(attempt);
    }

    private void validateAttemptPolicy(UUID studentId, UUID assessmentId, AssessmentConfiguration execConfig) {
        long attemptsUsed = countCompletedAttempts(studentId, assessmentId);
        if (attemptsUsed >= execConfig.getMaxAttempts()) {
            if (!execConfig.isAllowReattempt()) {
                throw new BusinessException("Maximum attempts exceeded", "MAX_ATTEMPTS_EXCEEDED");
            }
            if (execConfig.getReattemptAfterDays() != null && execConfig.getReattemptAfterDays() > 0) {
                attemptRepository.findTopByStudentIdAndAssessmentIdAndStatusOrderBySubmittedAtDesc(
                        studentId, assessmentId, AttemptStatus.SUBMITTED
                ).ifPresent(lastSubmitted -> {
                    if (lastSubmitted.getSubmittedAt() != null) {
                        long daysSince = ChronoUnit.DAYS.between(lastSubmitted.getSubmittedAt(), Instant.now());
                        if (daysSince < execConfig.getReattemptAfterDays()) {
                            throw new BusinessException("Reattempt not yet allowed", "MAX_ATTEMPTS_EXCEEDED");
                        }
                    }
                });
            }
        }
    }

    private AssessmentAttempt createAttempt(
            UUID studentId,
            AssessmentConfigurationGroupItem groupItem,
            AssessmentConfiguration execConfig,
            UUID languageOverrideId,
            StartAssessmentRequest request,
            UserPrincipal principal
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Language assessmentLanguage = resolveLanguage(languageOverrideId, groupItem, student);
        Language outputLanguage = resolveOutputLanguage(groupItem.getConfigurationGroup().getId(), student);

        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setTenant(tenant);
        attempt.setStudent(student);
        attempt.setAssessment(groupItem.getAssessment());
        attempt.setAssessmentVersion(groupItem.getAssessmentVersion());
        attempt.setConfigurationGroup(groupItem.getConfigurationGroup());
        attempt.setQuestionnaireVersion(groupItem.getQuestionnaireVersion());
        attempt.setIarLogicVersion(resolveIarLogicVersion(groupItem.getIarLogicVersionId()));
        attempt.setScoringRuleVersion(resolveScoringRuleVersion(groupItem.getScoringRuleVersionId()));
        attempt.setReportTemplateVersion(resolveReportTemplateVersion(groupItem.getConfigurationGroup().getId()));
        attempt.setAssessmentLanguage(assessmentLanguage);
        attempt.setOutputLanguage(outputLanguage);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setTimerMode(execConfig.getTimerMode());
        attempt.setStartedAt(Instant.now());
        attempt.setScoreStatus(ScoreStatus.PENDING);

        boolean startedByStaff = !principal.getId().equals(studentId)
                && (principal.hasRole(RoleName.ADMINISTRATOR)
                || principal.hasRole(RoleName.CAREER_COUNSELLOR)
                || principal.hasRole(RoleName.SUB_ADMIN));
        boolean adminTest = (request != null && Boolean.TRUE.equals(request.isAdminTestAttempt())) || startedByStaff;
        attempt.setAdminTestAttempt(adminTest);
        attempt.setExcludeFromAnalytics(request != null && Boolean.TRUE.equals(request.excludeFromAnalytics()));

        attempt = attemptRepository.save(attempt);
        statusHistoryService.record(attempt, null, AttemptStatus.IN_PROGRESS, "Started");
        return attempt;
    }

    private Language resolveLanguage(UUID overrideId, AssessmentConfigurationGroupItem groupItem, User student) {
        if (overrideId != null) {
            return languageRepository.findById(overrideId)
                    .orElseThrow(() -> new ResourceNotFoundException("Language not found"));
        }
        if (student.getPreferredAssessmentLanguageId() != null) {
            return languageRepository.findById(student.getPreferredAssessmentLanguageId())
                    .orElse(groupItem.getAssessmentLanguage());
        }
        return groupItem.getAssessmentLanguage();
    }

    private Language resolveOutputLanguage(UUID groupId, User student) {
        List<AssessmentConfigurationGroupOutput> outputs =
                outputRepository.findByConfigurationGroupIdAndIsActiveTrue(groupId);
        if (!outputs.isEmpty()) {
            return outputs.getFirst().getOutputLanguage();
        }
        if (student.getPreferredPlatformLanguageId() != null) {
            return languageRepository.findById(student.getPreferredPlatformLanguageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Output language not found"));
        }
        return languageRepository.findByIsDefaultTrueAndIsDeletedFalse()
                .orElseThrow(() -> new BusinessException("Output language not configured", "CONFIGURATION_NOT_FOUND"));
    }

    private IarLogicVersion resolveIarLogicVersion(UUID versionId) {
        if (versionId == null) {
            return null;
        }
        return iarLogicVersionRepository.findById(versionId).orElse(null);
    }

    private ScoringRuleVersion resolveScoringRuleVersion(UUID versionId) {
        if (versionId == null) {
            return null;
        }
        return scoringRuleVersionRepository.findById(versionId).orElse(null);
    }

    private ReportTemplateVersion resolveReportTemplateVersion(UUID groupId) {
        return outputRepository.findByConfigurationGroupIdAndIsActiveTrue(groupId).stream()
                .map(AssessmentConfigurationGroupOutput::getReportTemplateVersionId)
                .filter(id -> id != null)
                .findFirst()
                .flatMap(reportTemplateVersionRepository::findById)
                .orElse(null);
    }

    private StartAssessmentResponse buildStartResponse(
            AssessmentAttempt attempt,
            AssessmentConfiguration execConfig,
            boolean resumed
    ) {
        List<AttemptQuestionResponse> questions = buildQuestions(attempt, loadResponsesMap(attempt.getId()));
        return new StartAssessmentResponse(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getConfigurationGroup().getId(),
                attempt.getQuestionnaireVersion().getId(),
                attempt.getIarLogicVersion() != null ? attempt.getIarLogicVersion().getId() : null,
                attempt.getScoringRuleVersion() != null ? attempt.getScoringRuleVersion().getId() : null,
                attempt.getReportTemplateVersion() != null ? attempt.getReportTemplateVersion().getId() : null,
                attempt.getAssessmentLanguage().getId(),
                buildTimerResponse(attempt, execConfig),
                questions,
                resumed
        );
    }

    private AttemptDetailResponse buildAttemptDetail(AssessmentAttempt attempt, AssessmentConfiguration execConfig) {
        List<AttemptQuestionResponse> questions = buildQuestions(attempt, loadResponsesMap(attempt.getId()));
        return new AttemptDetailResponse(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getAssessment().getCode().name(),
                attempt.getAssessment().getName(),
                attempt.getConfigurationGroup().getId(),
                attempt.getQuestionnaireVersion().getId(),
                attempt.getAssessmentLanguage().getId(),
                attempt.getStatus(),
                attempt.getTimerMode(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                attempt.getElapsedSeconds(),
                attempt.getSubmissionReason(),
                attempt.getScoreStatus(),
                buildTimerResponse(attempt, execConfig),
                questions
        );
    }

    private AttemptTimerResponse buildTimerResponse(AssessmentAttempt attempt, AssessmentConfiguration execConfig) {
        return new AttemptTimerResponse(
                attempt.getTimerMode(),
                attempt.getStartedAt(),
                execConfig != null && execConfig.isShowTimerToStudent(),
                execConfig != null && execConfig.isAutoSubmitEnabled(),
                execConfig != null ? execConfig.getMaxDurationMinutes() : null,
                execConfig != null && execConfig.isAllowResume()
        );
    }

    private List<AttemptQuestionResponse> buildQuestions(
            AssessmentAttempt attempt,
            Map<UUID, AssessmentResponse> responsesByQuestionId
    ) {
        UUID languageId = attempt.getAssessmentLanguage().getId();
        List<QuestionnaireQuestion> links = questionnaireQuestionRepository
                .findByQuestionnaireVersionIdOrderByDisplayOrderAsc(attempt.getQuestionnaireVersion().getId());

        Map<UUID, Map<String, Object>> evaluatorContext = buildEvaluatorContext(responsesByQuestionId);

        List<AttemptQuestionResponse> questions = new ArrayList<>();
        for (QuestionnaireQuestion link : links) {
            Question question = link.getQuestion();
            if (!questionRuleEvaluator.isQuestionVisible(question, link.getConditionConfig(), evaluatorContext)) {
                continue;
            }
            String questionText = question.getDefaultText();
            QuestionTranslation translation = questionTranslationRepository
                    .findByQuestionIdAndLanguageId(question.getId(), languageId)
                    .orElse(null);
            if (translation != null) {
                questionText = translation.getQuestionText();
            }

            List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId());
            List<AttemptQuestionOptionResponse> optionResponses = new ArrayList<>();
            for (QuestionOption option : options) {
                if (!option.isActive()) {
                    continue;
                }
                String optionText = option.getDefaultText();
                OptionTranslation ot = optionTranslationRepository
                        .findByOptionIdAndLanguageId(option.getId(), languageId)
                        .orElse(null);
                if (ot != null) {
                    optionText = ot.getOptionText();
                }
                optionResponses.add(new AttemptQuestionOptionResponse(
                        option.getId(),
                        optionText,
                        option.getDisplayOrder()
                ));
            }

            AssessmentResponse saved = responsesByQuestionId.get(question.getId());
            List<UUID> selectedOptionIds = saved != null
                    ? responseOptionRepository.findByResponseId(saved.getId()).stream()
                    .map(ro -> ro.getOption().getId())
                    .toList()
                    : List.of();

            questions.add(new AttemptQuestionResponse(
                    question.getId(),
                    question.getQuestionType(),
                    questionText,
                    link.isMandatory(),
                    link.getDisplayOrder(),
                    link.getSectionCode(),
                    optionResponses,
                    link.getConditionConfig(),
                    selectedOptionIds,
                    saved != null ? saved.getResponseText() : null,
                    saved != null ? saved.getResponseNumeric() : null,
                    saved != null && saved.isSkipped()
            ));
        }
        return questions;
    }

    private Map<UUID, AssessmentResponse> loadResponsesMap(UUID attemptId) {
        return responseRepository.findByAttemptId(attemptId).stream()
                .collect(Collectors.toMap(r -> r.getQuestion().getId(), r -> r));
    }

    private void validateMandatoryResponses(AssessmentAttempt attempt) {
        AssessmentConfiguration execConfig = configurationRepository
                .findByAssessmentVersionId(attempt.getAssessmentVersion().getId())
                .orElse(null);
        if (execConfig != null && execConfig.getCompletionRule() == CompletionRule.ALLOW_PARTIAL) {
            return;
        }

        List<QuestionnaireQuestion> links = questionnaireQuestionRepository
                .findByQuestionnaireVersionIdOrderByDisplayOrderAsc(attempt.getQuestionnaireVersion().getId());
        Map<UUID, AssessmentResponse> responses = loadResponsesMap(attempt.getId());
        Map<UUID, Map<String, Object>> evaluatorContext = buildEvaluatorContext(responses);
        List<UUID> missing = new ArrayList<>();

        for (QuestionnaireQuestion link : links) {
            Question question = link.getQuestion();
            if (!link.isMandatory()) {
                continue;
            }
            if (!questionRuleEvaluator.isQuestionVisible(question, link.getConditionConfig(), evaluatorContext)) {
                continue;
            }
            AssessmentResponse response = responses.get(question.getId());
            if (response == null || response.isSkipped()) {
                missing.add(question.getId());
                continue;
            }
            boolean hasSelection = !responseOptionRepository.findByResponseId(response.getId()).isEmpty();
            boolean hasText = response.getResponseText() != null && !response.getResponseText().isBlank();
            boolean hasNumeric = response.getResponseNumeric() != null;
            if (!hasSelection && !hasText && !hasNumeric) {
                missing.add(question.getId());
            }
        }

        if (!missing.isEmpty()) {
            Map<String, Object> data = Map.of("missingQuestionIds", missing);
            throw new BusinessException("Mandatory questions are unanswered", "MANDATORY_RESPONSE_MISSING", data);
        }
    }

    private Map<UUID, Map<String, Object>> buildEvaluatorContext(Map<UUID, AssessmentResponse> responsesByQuestionId) {
        Map<UUID, Map<String, Object>> evaluatorContext = new HashMap<>();
        for (Map.Entry<UUID, AssessmentResponse> entry : responsesByQuestionId.entrySet()) {
            AssessmentResponse response = entry.getValue();
            List<UUID> selected = responseOptionRepository.findByResponseId(response.getId()).stream()
                    .map(ro -> ro.getOption().getId())
                    .toList();
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("selectedOptionIds", selected);
            ctx.put("responseText", response.getResponseText());
            ctx.put("responseNumeric", response.getResponseNumeric());
            ctx.put("isSkipped", response.isSkipped());
            evaluatorContext.put(entry.getKey(), ctx);
        }
        return evaluatorContext;
    }

    private AssessmentAttempt requireAttempt(UUID attemptId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        return attemptRepository.findByIdAndTenantId(attemptId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
    }

    private Map<UUID, Integer> computeAttemptNumbers(UUID studentId, List<AssessmentAttempt> attempts) {
        Map<UUID, Integer> numbers = new HashMap<>();
        Set<UUID> assessmentIds = attempts.stream()
                .map(a -> a.getAssessment().getId())
                .collect(Collectors.toSet());
        for (UUID assessmentId : assessmentIds) {
            List<AssessmentAttempt> all = attemptRepository
                    .findByStudentIdAndAssessmentIdOrderByStartedAtDesc(
                            studentId, assessmentId, PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "startedAt"))
                    ).getContent();
            for (int i = 0; i < all.size(); i++) {
                numbers.put(all.get(i).getId(), i + 1);
            }
        }
        return numbers;
    }

    private AttemptHistoryItemResponse toHistoryItem(AssessmentAttempt attempt, int attemptNumber) {
        return new AttemptHistoryItemResponse(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getAssessment().getCode().name(),
                attempt.getAssessment().getName(),
                attemptNumber,
                attempt.getStatus(),
                attempt.getScoreStatus(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                attempt.getElapsedSeconds(),
                attempt.getSubmissionReason()
        );
    }
}
