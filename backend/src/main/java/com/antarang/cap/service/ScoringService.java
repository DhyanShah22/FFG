package com.antarang.cap.service;

import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.AssessmentResponse;
import com.antarang.cap.domain.entity.AssessmentResponseOption;
import com.antarang.cap.domain.entity.Benchmark;
import com.antarang.cap.domain.entity.DomainScore;
import com.antarang.cap.domain.entity.Question;
import com.antarang.cap.domain.entity.ScoringRuleVersion;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.entity.UserProfile;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.ScoreStatus;
import com.antarang.cap.dto.response.CalculateScoringResponse;
import com.antarang.cap.dto.response.DomainScoreResponse;
import com.antarang.cap.dto.response.StudentScoresResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import com.antarang.cap.repository.AssessmentResponseOptionRepository;
import com.antarang.cap.repository.AssessmentResponseRepository;
import com.antarang.cap.repository.BenchmarkRepository;
import com.antarang.cap.repository.DomainScoreRepository;
import com.antarang.cap.repository.UserProfileRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.StudentAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ScoringService {

    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentResponseRepository responseRepository;
    private final AssessmentResponseOptionRepository responseOptionRepository;
    private final DomainScoreRepository domainScoreRepository;
    private final BenchmarkRepository benchmarkRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudentAccessGuard studentAccessGuard;

    public ScoringService(
            AssessmentAttemptRepository attemptRepository,
            AssessmentResponseRepository responseRepository,
            AssessmentResponseOptionRepository responseOptionRepository,
            DomainScoreRepository domainScoreRepository,
            BenchmarkRepository benchmarkRepository,
            UserProfileRepository userProfileRepository,
            StudentAccessGuard studentAccessGuard
    ) {
        this.attemptRepository = attemptRepository;
        this.responseRepository = responseRepository;
        this.responseOptionRepository = responseOptionRepository;
        this.domainScoreRepository = domainScoreRepository;
        this.benchmarkRepository = benchmarkRepository;
        this.userProfileRepository = userProfileRepository;
        this.studentAccessGuard = studentAccessGuard;
    }

    @Transactional
    public CalculateScoringResponse calculate(UUID attemptId, boolean force) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AssessmentAttempt attempt = attemptRepository.findByIdAndTenantId(attemptId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.SUBMITTED && attempt.getStatus() != AttemptStatus.EVALUATED) {
            throw new BusinessException("Attempt must be submitted before scoring", "INVALID_STATE");
        }
        if (!force && attempt.getScoreStatus() == ScoreStatus.COMPLETED) {
            return new CalculateScoringResponse(attemptId, loadDomainScoreResponses(attemptId));
        }

        List<DomainScoreResponse> scores;
        try {
            scores = performScoring(attempt);
            attempt.setScoreStatus(ScoreStatus.COMPLETED);
            attempt.setEvaluatedAt(Instant.now());
        } catch (BusinessException ex) {
            attempt.setScoreStatus(ScoreStatus.FAILED);
            throw ex;
        } catch (RuntimeException ex) {
            attempt.setScoreStatus(ScoreStatus.FAILED);
            throw new BusinessException("Scoring failed: " + ex.getMessage(), "SCORING_RULE_MISSING");
        }
        attemptRepository.save(attempt);
        return new CalculateScoringResponse(attemptId, scores);
    }

    @Transactional(readOnly = true)
    public StudentScoresResponse getStudentScores(UUID studentId, UUID attemptId) {
        studentAccessGuard.assertStudentAccess(studentId);
        UUID tenantId = SecurityUtils.requireTenantId();

        AssessmentAttempt attempt;
        if (attemptId != null) {
            attempt = attemptRepository.findByIdAndTenantId(attemptId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
            if (!attempt.getStudent().getId().equals(studentId)) {
                throw new BusinessException("Attempt does not belong to student", "ACCESS_DENIED");
            }
        } else {
            attempt = attemptRepository
                    .findTopByStudentIdAndStatusOrderBySubmittedAtDesc(studentId, AttemptStatus.SUBMITTED)
                    .orElseThrow(() -> new ResourceNotFoundException("No submitted attempt with scores found"));
        }

        List<DomainScoreResponse> scores = loadDomainScoreResponses(attempt.getId());
        return new StudentScoresResponse(studentId, attempt.getId(), scores);
    }

    @Transactional
    public List<DomainScoreResponse> scoreOnSubmit(AssessmentAttempt attempt) {
        List<DomainScoreResponse> scores = performScoring(attempt);
        attempt.setScoreStatus(ScoreStatus.COMPLETED);
        attempt.setEvaluatedAt(Instant.now());
        return scores;
    }

    @SuppressWarnings("unchecked")
    private List<DomainScoreResponse> performScoring(AssessmentAttempt attempt) {
        ScoringRuleVersion scoringRuleVersion = attempt.getScoringRuleVersion();
        if (scoringRuleVersion == null || scoringRuleVersion.getRuleDefinition() == null) {
            throw new BusinessException("Scoring rule version is missing on attempt", "SCORING_RULE_MISSING");
        }

        Map<String, Object> ruleDefinition = scoringRuleVersion.getRuleDefinition();
        Object domainsRaw = ruleDefinition.get("domains");
        if (!(domainsRaw instanceof List<?> domains)) {
            throw new BusinessException("Invalid scoring rule definition", "SCORING_RULE_MISSING");
        }

        Map<String, BigDecimal> questionCodeScores = buildQuestionCodeScoreMap(attempt.getId());
        UserProfile profile = userProfileRepository.findByUserId(attempt.getStudent().getId()).orElse(null);
        UUID gradeConfigId = profile != null ? profile.getGradeConfigId() : null;
        UUID ageGroupConfigId = profile != null ? profile.getAgeGroupConfigId() : null;

        List<DomainScoreResponse> results = new ArrayList<>();
        for (Object domainRaw : domains) {
            if (!(domainRaw instanceof Map<?, ?> domainMap)) {
                continue;
            }
            String domainCode = String.valueOf(domainMap.get("domainCode"));
            Object questionCodesRaw = domainMap.get("questionCodes");
            Set<String> questionCodes = new HashSet<>();
            if (questionCodesRaw instanceof List<?> codes) {
                codes.forEach(code -> questionCodes.add(String.valueOf(code)));
            }

            BigDecimal rawScore = BigDecimal.ZERO;
            for (String questionCode : questionCodes) {
                rawScore = rawScore.add(questionCodeScores.getOrDefault(questionCode, BigDecimal.ZERO));
            }

            Benchmark benchmark = matchBenchmark(
                    attempt.getTenant().getId(),
                    attempt.getAssessment().getId(),
                    domainCode,
                    gradeConfigId,
                    ageGroupConfigId,
                    rawScore
            );

            upsertDomainScore(attempt, domainCode, rawScore, rawScore, benchmark);
            results.add(new DomainScoreResponse(
                    domainCode,
                    rawScore,
                    rawScore,
                    benchmark != null ? benchmark.getBenchmarkLabel() : null,
                    benchmark != null ? benchmark.getInterpretation() : null
            ));
        }
        return results;
    }

    private Map<String, BigDecimal> buildQuestionCodeScoreMap(UUID attemptId) {
        Map<String, BigDecimal> scores = new HashMap<>();
        List<AssessmentResponse> responses = responseRepository.findByAttemptId(attemptId);
        for (AssessmentResponse response : responses) {
            if (response.isSkipped()) {
                continue;
            }
            Question question = response.getQuestion();
            List<AssessmentResponseOption> options = responseOptionRepository.findByResponseId(response.getId());
            BigDecimal sum = BigDecimal.ZERO;
            for (AssessmentResponseOption optionLink : options) {
                if (optionLink.getOption().getScoreValue() != null) {
                    sum = sum.add(optionLink.getOption().getScoreValue());
                }
            }
            scores.put(question.getQuestionCode(), sum);
        }
        return scores;
    }

    private Benchmark matchBenchmark(
            UUID tenantId,
            UUID assessmentId,
            String domainCode,
            UUID gradeConfigId,
            UUID ageGroupConfigId,
            BigDecimal rawScore
    ) {
        List<Benchmark> benchmarks = benchmarkRepository.findWithFilters(
                tenantId, assessmentId, domainCode, gradeConfigId, ageGroupConfigId, null, true
        );
        for (Benchmark benchmark : benchmarks) {
            if (rawScore.compareTo(benchmark.getMinScore()) >= 0
                    && rawScore.compareTo(benchmark.getMaxScore()) <= 0) {
                return benchmark;
            }
        }
        return null;
    }

    private void upsertDomainScore(
            AssessmentAttempt attempt,
            String domainCode,
            BigDecimal rawScore,
            BigDecimal normalizedScore,
            Benchmark benchmark
    ) {
        User student = attempt.getStudent();
        List<DomainScore> existing = domainScoreRepository.findByAttemptId(attempt.getId());
        DomainScore domainScore = existing.stream()
                .filter(score -> score.getDomainCode().equals(domainCode))
                .findFirst()
                .orElseGet(() -> {
                    DomainScore created = new DomainScore();
                    created.setAttempt(attempt);
                    created.setStudent(student);
                    created.setAssessment(attempt.getAssessment());
                    created.setDomainCode(domainCode);
                    return created;
                });
        domainScore.setRawScore(rawScore);
        domainScore.setNormalizedScore(normalizedScore);
        domainScore.setBenchmark(benchmark);
        domainScore.setCalculatedAt(Instant.now());
        domainScoreRepository.save(domainScore);
    }

    private List<DomainScoreResponse> loadDomainScoreResponses(UUID attemptId) {
        return domainScoreRepository.findByAttemptId(attemptId).stream()
                .map(score -> new DomainScoreResponse(
                        score.getDomainCode(),
                        score.getRawScore(),
                        score.getNormalizedScore(),
                        score.getBenchmark() != null ? score.getBenchmark().getBenchmarkLabel() : null,
                        score.getBenchmark() != null ? score.getBenchmark().getInterpretation() : null
                ))
                .toList();
    }
}
