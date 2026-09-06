package com.antarang.cap.service;

import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.AssessmentQuestionTiming;
import com.antarang.cap.domain.entity.Question;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.entity.QuestionnaireQuestion;
import com.antarang.cap.dto.request.QuestionTimingItemRequest;
import com.antarang.cap.dto.request.UpsertQuestionTimingRequest;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import com.antarang.cap.repository.AssessmentQuestionTimingRepository;
import com.antarang.cap.repository.QuestionnaireQuestionRepository;
import com.antarang.cap.repository.QuestionRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.StudentAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class QuestionTimingService {

    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentQuestionTimingRepository timingRepository;
    private final QuestionnaireQuestionRepository questionnaireQuestionRepository;
    private final QuestionRepository questionRepository;
    private final StudentAccessGuard studentAccessGuard;
    private final AttemptExpiryGuard attemptExpiryGuard;

    public QuestionTimingService(
            AssessmentAttemptRepository attemptRepository,
            AssessmentQuestionTimingRepository timingRepository,
            QuestionnaireQuestionRepository questionnaireQuestionRepository,
            QuestionRepository questionRepository,
            StudentAccessGuard studentAccessGuard,
            AttemptExpiryGuard attemptExpiryGuard
    ) {
        this.attemptRepository = attemptRepository;
        this.timingRepository = timingRepository;
        this.questionnaireQuestionRepository = questionnaireQuestionRepository;
        this.questionRepository = questionRepository;
        this.studentAccessGuard = studentAccessGuard;
        this.attemptExpiryGuard = attemptExpiryGuard;
    }

    @Transactional
    public void upsertTimings(UUID attemptId, UpsertQuestionTimingRequest request) {
        AssessmentAttempt attempt = requireInProgressAttempt(attemptId);
        studentAccessGuard.assertAttemptOwner(attempt);
        attemptExpiryGuard.assertWritable(attempt);

        List<QuestionTimingItemRequest> items = new ArrayList<>();
        if (request.timings() != null && !request.timings().isEmpty()) {
            items.addAll(request.timings());
        } else if (request.questionId() != null) {
            items.add(new QuestionTimingItemRequest(
                    request.questionId(),
                    request.startedAt(),
                    request.endedAt(),
                    request.elapsedSeconds()
            ));
        } else {
            throw new BusinessException("At least one timing entry is required", "VALIDATION_ERROR");
        }

        Set<UUID> versionQuestionIds = versionQuestionIds(attempt);
        for (QuestionTimingItemRequest item : items) {
            if (!versionQuestionIds.contains(item.questionId())) {
                throw new BusinessException("Question does not belong to this attempt", "VALIDATION_ERROR");
            }
            Question question = questionRepository.findById(item.questionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
            AssessmentQuestionTiming timing = timingRepository
                    .findByAttemptIdAndQuestionId(attemptId, item.questionId())
                    .orElseGet(() -> {
                        AssessmentQuestionTiming created = new AssessmentQuestionTiming();
                        created.setAttempt(attempt);
                        created.setQuestion(question);
                        return created;
                    });
            timing.setStartedAt(item.startedAt());
            timing.setEndedAt(item.endedAt());
            timing.setElapsedSeconds(item.elapsedSeconds());
            timingRepository.save(timing);
        }
    }

    private AssessmentAttempt requireInProgressAttempt(UUID attemptId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AssessmentAttempt attempt = attemptRepository.findByIdAndTenantId(attemptId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BusinessException("Attempt is not in progress", "ATTEMPT_NOT_IN_PROGRESS");
        }
        return attempt;
    }

    private Set<UUID> versionQuestionIds(AssessmentAttempt attempt) {
        List<QuestionnaireQuestion> links = questionnaireQuestionRepository
                .findByQuestionnaireVersionIdOrderByDisplayOrderAsc(attempt.getQuestionnaireVersion().getId());
        Set<UUID> ids = new HashSet<>();
        for (QuestionnaireQuestion link : links) {
            ids.add(link.getQuestion().getId());
        }
        return ids;
    }
}
