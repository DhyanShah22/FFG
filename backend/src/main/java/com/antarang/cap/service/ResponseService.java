package com.antarang.cap.service;

import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.AssessmentResponse;
import com.antarang.cap.domain.entity.AssessmentResponseOption;
import com.antarang.cap.domain.entity.Question;
import com.antarang.cap.domain.entity.QuestionOption;
import com.antarang.cap.domain.entity.QuestionnaireQuestion;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.QuestionType;
import com.antarang.cap.dto.request.AttemptResponseItemRequest;
import com.antarang.cap.dto.request.UpsertAttemptResponsesRequest;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import com.antarang.cap.repository.AssessmentResponseOptionRepository;
import com.antarang.cap.repository.AssessmentResponseRepository;
import com.antarang.cap.repository.QuestionOptionRepository;
import com.antarang.cap.repository.QuestionnaireQuestionRepository;
import com.antarang.cap.repository.QuestionRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.StudentAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ResponseService {

    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentResponseRepository responseRepository;
    private final AssessmentResponseOptionRepository responseOptionRepository;
    private final QuestionnaireQuestionRepository questionnaireQuestionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final StudentAccessGuard studentAccessGuard;
    private final AttemptExpiryGuard attemptExpiryGuard;

    public ResponseService(
            AssessmentAttemptRepository attemptRepository,
            AssessmentResponseRepository responseRepository,
            AssessmentResponseOptionRepository responseOptionRepository,
            QuestionnaireQuestionRepository questionnaireQuestionRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            StudentAccessGuard studentAccessGuard,
            AttemptExpiryGuard attemptExpiryGuard
    ) {
        this.attemptRepository = attemptRepository;
        this.responseRepository = responseRepository;
        this.responseOptionRepository = responseOptionRepository;
        this.questionnaireQuestionRepository = questionnaireQuestionRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.studentAccessGuard = studentAccessGuard;
        this.attemptExpiryGuard = attemptExpiryGuard;
    }

    @Transactional
    public void upsertResponses(UUID attemptId, UpsertAttemptResponsesRequest request) {
        AssessmentAttempt attempt = requireInProgressAttempt(attemptId);
        studentAccessGuard.assertAttemptOwner(attempt);
        attemptExpiryGuard.assertWritable(attempt);
        Set<UUID> versionQuestionIds = versionQuestionIds(attempt);

        for (AttemptResponseItemRequest item : request.responses()) {
            if (!versionQuestionIds.contains(item.questionId())) {
                throw new BusinessException("Question does not belong to this attempt", "VALIDATION_ERROR");
            }
            Question question = questionRepository.findById(item.questionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

            List<UUID> selectedOptionIds = item.selectedOptionIds() != null ? item.selectedOptionIds() : List.of();
            validateOptions(question, selectedOptionIds);

            AssessmentResponse response = responseRepository
                    .findByAttemptIdAndQuestionId(attemptId, item.questionId())
                    .orElseGet(() -> {
                        AssessmentResponse created = new AssessmentResponse();
                        created.setAttempt(attempt);
                        created.setQuestion(question);
                        return created;
                    });

            boolean skipped = Boolean.TRUE.equals(item.isSkipped());
            response.setSkipped(skipped);
            response.setResponseText(item.responseText());
            response.setResponseNumeric(item.responseNumeric());
            response.setAnsweredAt(Instant.now());
            response = responseRepository.save(response);

            responseOptionRepository.deleteByResponseId(response.getId());
            if (!skipped && !selectedOptionIds.isEmpty()) {
                for (UUID optionId : selectedOptionIds) {
                    QuestionOption option = questionOptionRepository.findById(optionId)
                            .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
                    AssessmentResponseOption responseOption = new AssessmentResponseOption();
                    responseOption.setResponse(response);
                    responseOption.setOption(option);
                    responseOptionRepository.save(responseOption);
                }
            }
        }
    }

    private void validateOptions(Question question, List<UUID> selectedOptionIds) {
        if (selectedOptionIds.isEmpty()) {
            return;
        }
        if (question.getQuestionType() == QuestionType.RADIO && selectedOptionIds.size() > 1) {
            throw new BusinessException("RADIO questions allow at most one selected option", "VALIDATION_ERROR");
        }
        for (UUID optionId : selectedOptionIds) {
            QuestionOption option = questionOptionRepository.findById(optionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
            if (!option.getQuestion().getId().equals(question.getId())) {
                throw new BusinessException("Option does not belong to question", "VALIDATION_ERROR");
            }
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
