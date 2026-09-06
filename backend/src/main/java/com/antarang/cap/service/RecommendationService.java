package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.Career;
import com.antarang.cap.domain.entity.CareerMapping;
import com.antarang.cap.domain.entity.DomainScore;
import com.antarang.cap.domain.entity.IarLogicVersion;
import com.antarang.cap.domain.entity.Recommendation;
import com.antarang.cap.domain.entity.RecommendationRun;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.BenchmarkLabel;
import com.antarang.cap.domain.enums.RecommendationRunStatus;
import com.antarang.cap.domain.enums.ScoreStatus;
import com.antarang.cap.dto.request.GenerateRecommendationRequest;
import com.antarang.cap.dto.response.GenerateRecommendationResponse;
import com.antarang.cap.dto.response.RecommendationItemResponse;
import com.antarang.cap.dto.response.RecommendationRunSummaryResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import com.antarang.cap.repository.CareerMappingRepository;
import com.antarang.cap.repository.DomainScoreRepository;
import com.antarang.cap.repository.RecommendationRepository;
import com.antarang.cap.repository.RecommendationRunRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.StudentAccessGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final int MAX_RECOMMENDATIONS = 10;

    private final RecommendationRunRepository recommendationRunRepository;
    private final RecommendationRepository recommendationRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final DomainScoreRepository domainScoreRepository;
    private final CareerMappingRepository careerMappingRepository;
    private final UserRepository userRepository;
    private final StudentAccessGuard studentAccessGuard;

    public RecommendationService(
            RecommendationRunRepository recommendationRunRepository,
            RecommendationRepository recommendationRepository,
            AssessmentAttemptRepository attemptRepository,
            DomainScoreRepository domainScoreRepository,
            CareerMappingRepository careerMappingRepository,
            UserRepository userRepository,
            StudentAccessGuard studentAccessGuard
    ) {
        this.recommendationRunRepository = recommendationRunRepository;
        this.recommendationRepository = recommendationRepository;
        this.attemptRepository = attemptRepository;
        this.domainScoreRepository = domainScoreRepository;
        this.careerMappingRepository = careerMappingRepository;
        this.userRepository = userRepository;
        this.studentAccessGuard = studentAccessGuard;
    }

    @Transactional
    public GenerateRecommendationResponse generate(UUID studentId, GenerateRecommendationRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        studentAccessGuard.assertStudentAccess(studentId);

        User student = userRepository.findById(studentId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        SecurityUtils.assertSameTenant(student.getTenant().getId());

        AssessmentAttempt attempt = resolveAttempt(studentId, request != null ? request.attemptId() : null, tenantId);
        List<DomainScore> domainScores = domainScoreRepository.findByAttemptId(attempt.getId());
        if (domainScores.isEmpty()) {
            throw new BusinessException("Cannot generate recommendations without domain scores", "NO_SCORES");
        }

        IarLogicVersion iarVersion = attempt.getIarLogicVersion();
        if (iarVersion == null) {
            throw new BusinessException("Attempt has no IAR logic version snapshot", "INVALID_STATE");
        }

        Map<String, DomainScore> scoreByDomain = domainScores.stream()
                .collect(Collectors.toMap(DomainScore::getDomainCode, ds -> ds, (a, b) -> a));

        Map<String, Double> iarWeights = extractIarWeights(iarVersion.getLogicDefinition());
        List<CareerMapping> mappings = careerMappingRepository.findByTenantIdAndIsActiveTrue(tenantId);

        Map<UUID, BigDecimal> careerScores = new LinkedHashMap<>();
        Map<UUID, Map<String, Object>> careerBreakdowns = new HashMap<>();
        Map<UUID, Career> careerEntities = new HashMap<>();

        for (CareerMapping mapping : mappings) {
            Career career = mapping.getCareer();
            if (!career.isActive()) {
                continue;
            }

            BigDecimal mappingScore = computeMappingScore(mapping, scoreByDomain, iarWeights);
            if (mappingScore == null) {
                continue;
            }

            careerEntities.putIfAbsent(career.getId(), career);
            careerScores.merge(career.getId(), mappingScore, BigDecimal::add);
            careerBreakdowns.put(career.getId(), buildMatchBreakdown(mapping, scoreByDomain));
        }

        List<Map.Entry<UUID, BigDecimal>> ranked = careerScores.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(MAX_RECOMMENDATIONS)
                .toList();

        RecommendationRun run = new RecommendationRun();
        run.setStudent(student);
        run.setConfigurationGroup(attempt.getConfigurationGroup());
        run.setIarLogicVersion(iarVersion);
        run.setStatus(RecommendationRunStatus.COMPLETED);
        run.setGeneratedBy(SecurityUtils.requirePrincipal().getId());
        RecommendationRun savedRun = recommendationRunRepository.save(run);

        List<RecommendationItemResponse> items = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<UUID, BigDecimal> entry : ranked) {
            Career career = careerEntities.get(entry.getKey());
            BigDecimal normalizedScore = entry.getValue().setScale(0, RoundingMode.HALF_UP);
            if (normalizedScore.compareTo(BigDecimal.valueOf(100)) > 0) {
                normalizedScore = BigDecimal.valueOf(100);
            }

            Map<String, Object> breakdown = careerBreakdowns.getOrDefault(entry.getKey(), Map.of());
            String reason = buildReason(career.getName(), breakdown);

            Recommendation rec = new Recommendation();
            rec.setRecommendationRun(savedRun);
            rec.setCareer(career);
            rec.setRankOrder(rank);
            rec.setRecommendationScore(normalizedScore);
            rec.setRecommendationReason(reason);
            rec.setMatchBreakdown(breakdown);
            recommendationRepository.save(rec);

            items.add(new RecommendationItemResponse(
                    career.getId(),
                    career.getName(),
                    rank,
                    normalizedScore,
                    reason,
                    breakdown
            ));
            rank++;
        }

        if (items.isEmpty()) {
            savedRun.setStatus(RecommendationRunStatus.FAILED);
            recommendationRunRepository.save(savedRun);
        }

        return new GenerateRecommendationResponse(savedRun.getId(), studentId, items);
    }

    @Transactional(readOnly = true)
    public PageResponse<?> listRecommendations(UUID studentId, UUID runId, int page, int size) {
        studentAccessGuard.assertStudentAccess(studentId);

        if (runId != null) {
            RecommendationRun run = recommendationRunRepository.findByIdAndStudentId(runId, studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recommendation run not found"));
            Pageable pageable = PageRequest.of(page, size);
            Page<Recommendation> recPage = recommendationRepository
                    .findByRecommendationRunIdOrderByRankOrderAsc(run.getId(), pageable);
            return PageResponse.from(recPage.map(this::toItemResponse));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<RecommendationRun> runs = recommendationRunRepository
                .findByStudentIdOrderByGeneratedAtDesc(studentId, pageable);

        if (page == 0 && runs.hasContent()) {
            RecommendationRun latest = runs.getContent().getFirst();
            List<RecommendationItemResponse> items = recommendationRepository
                    .findByRecommendationRunIdOrderByRankOrderAsc(latest.getId()).stream()
                    .map(this::toItemResponse)
                    .toList();
            if (!items.isEmpty()) {
                return new PageResponse<>(items, page, size, items.size(), 1, true);
            }
        }

        return PageResponse.from(runs.map(run -> new RecommendationRunSummaryResponse(
                run.getId(),
                studentId,
                run.getConfigurationGroup().getId(),
                run.getIarLogicVersion().getId(),
                run.getStatus().name(),
                run.getGeneratedAt()
        )));
    }

    private AssessmentAttempt resolveAttempt(UUID studentId, UUID attemptId, UUID tenantId) {
        if (attemptId != null) {
            AssessmentAttempt attempt = attemptRepository.findByIdAndTenantId(attemptId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
            if (!attempt.getStudent().getId().equals(studentId)) {
                throw new BusinessException("Attempt does not belong to student", "ACCESS_DENIED");
            }
            if (attempt.getStatus() != AttemptStatus.SUBMITTED) {
                throw new BusinessException("Attempt must be SUBMITTED", "INVALID_STATE");
            }
            if (attempt.getScoreStatus() == ScoreStatus.PENDING) {
                throw new BusinessException("Attempt scores not yet calculated", "NO_SCORES");
            }
            return attempt;
        }

        return attemptRepository.findTopByStudentIdAndStatusOrderBySubmittedAtDesc(studentId, AttemptStatus.SUBMITTED)
                .filter(a -> a.getScoreStatus() != ScoreStatus.PENDING)
                .orElseThrow(() -> new BusinessException("No submitted attempt with scores found", "NO_SCORES"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Double> extractIarWeights(Map<String, Object> logicDefinition) {
        Map<String, Double> defaults = Map.of(
                "interest", 0.25,
                "aptitude", 0.25,
                "reality", 0.25,
                "aspiration", 0.25
        );
        if (logicDefinition == null) {
            return defaults;
        }
        Object weightsObj = logicDefinition.get("weights");
        if (!(weightsObj instanceof Map<?, ?> weights)) {
            return defaults;
        }
        Map<String, Double> result = new HashMap<>(defaults);
        for (String key : List.of("interest", "aptitude", "reality", "aspiration")) {
            Object val = weights.get(key);
            if (val instanceof Number number) {
                result.put(key, number.doubleValue());
            }
        }
        return result;
    }

    private BigDecimal computeMappingScore(
            CareerMapping mapping,
            Map<String, DomainScore> scoreByDomain,
            Map<String, Double> iarWeights
    ) {
        double totalWeight = 0;
        double weightedSum = 0;

        if (mapping.getInterestDomain() != null) {
            Double component = scoreComponent(scoreByDomain.get(mapping.getInterestDomain()), mapping);
            if (component != null) {
                weightedSum += component * iarWeights.getOrDefault("interest", 0.25);
                totalWeight += iarWeights.getOrDefault("interest", 0.25);
            }
        }
        if (mapping.getAptitudeDomain() != null) {
            Double component = scoreComponent(scoreByDomain.get(mapping.getAptitudeDomain()), mapping);
            if (component != null) {
                weightedSum += component * iarWeights.getOrDefault("aptitude", 0.25);
                totalWeight += iarWeights.getOrDefault("aptitude", 0.25);
            }
        }
        if (mapping.getRealityFactor() != null) {
            DomainScore realityScore = scoreByDomain.get(mapping.getRealityFactor());
            Double component = realityScore != null ? labelToScore(realityScore) : 66.0;
            weightedSum += component * iarWeights.getOrDefault("reality", 0.25);
            totalWeight += iarWeights.getOrDefault("reality", 0.25);
        }
        if (mapping.getAspirationFactor() != null) {
            DomainScore aspirationScore = scoreByDomain.get(mapping.getAspirationFactor());
            Double component = aspirationScore != null ? labelToScore(aspirationScore) : 66.0;
            weightedSum += component * iarWeights.getOrDefault("aspiration", 0.25);
            totalWeight += iarWeights.getOrDefault("aspiration", 0.25);
        }

        if (totalWeight == 0) {
            return null;
        }

        BigDecimal score = BigDecimal.valueOf(weightedSum / totalWeight);
        return score.multiply(mapping.getMappingWeight());
    }

    private Double scoreComponent(DomainScore domainScore, CareerMapping mapping) {
        if (domainScore == null) {
            return null;
        }
        BigDecimal score = domainScore.getNormalizedScore() != null
                ? domainScore.getNormalizedScore()
                : domainScore.getRawScore();
        if (mapping.getMinScore() != null && score.compareTo(mapping.getMinScore()) < 0) {
            return null;
        }
        if (mapping.getMaxScore() != null && score.compareTo(mapping.getMaxScore()) > 0) {
            return null;
        }
        return score.doubleValue();
    }

    private double labelToScore(DomainScore domainScore) {
        if (domainScore.getBenchmark() != null) {
            return switch (domainScore.getBenchmark().getBenchmarkLabel()) {
                case HIGH -> 100.0;
                case MEDIUM -> 66.0;
                case LOW -> 33.0;
            };
        }
        BigDecimal score = domainScore.getNormalizedScore() != null
                ? domainScore.getNormalizedScore()
                : domainScore.getRawScore();
        return score.doubleValue();
    }

    private Map<String, Object> buildMatchBreakdown(CareerMapping mapping, Map<String, DomainScore> scoreByDomain) {
        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("interest", labelForDomain(scoreByDomain.get(mapping.getInterestDomain())));
        breakdown.put("aptitude", labelForDomain(scoreByDomain.get(mapping.getAptitudeDomain())));
        breakdown.put("reality", mapping.getRealityFactor() != null
                ? labelForDomain(scoreByDomain.get(mapping.getRealityFactor()))
                : "MEDIUM");
        breakdown.put("aspiration", mapping.getAspirationFactor() != null
                ? aspirationMatch(scoreByDomain.get(mapping.getAspirationFactor()), mapping)
                : "MATCH");
        return breakdown;
    }

    private String labelForDomain(DomainScore domainScore) {
        if (domainScore == null) {
            return "MEDIUM";
        }
        if (domainScore.getBenchmark() != null) {
            BenchmarkLabel label = domainScore.getBenchmark().getBenchmarkLabel();
            return label.name();
        }
        BigDecimal score = domainScore.getNormalizedScore() != null
                ? domainScore.getNormalizedScore()
                : domainScore.getRawScore();
        if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "HIGH";
        }
        if (score.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String aspirationMatch(DomainScore domainScore, CareerMapping mapping) {
        if (domainScore == null) {
            return "PARTIAL";
        }
        BigDecimal score = domainScore.getNormalizedScore() != null
                ? domainScore.getNormalizedScore()
                : domainScore.getRawScore();
        if (mapping.getMinScore() != null && score.compareTo(mapping.getMinScore()) < 0) {
            return "MISMATCH";
        }
        if (mapping.getMaxScore() != null && score.compareTo(mapping.getMaxScore()) > 0) {
            return "MISMATCH";
        }
        return "MATCH";
    }

    private String buildReason(String careerName, Map<String, Object> breakdown) {
        List<String> strengths = new ArrayList<>();
        breakdown.forEach((key, value) -> {
            if ("HIGH".equals(value) || "MATCH".equals(value)) {
                strengths.add(key);
            }
        });
        if (strengths.isEmpty()) {
            return "Potential fit for " + careerName + " based on assessment profile";
        }
        return "Strong " + String.join(" and ", strengths) + " alignment for " + careerName;
    }

    private RecommendationItemResponse toItemResponse(Recommendation rec) {
        return new RecommendationItemResponse(
                rec.getCareer().getId(),
                rec.getCareer().getName(),
                rec.getRankOrder(),
                rec.getRecommendationScore(),
                rec.getRecommendationReason(),
                rec.getMatchBreakdown()
        );
    }
}
