package com.antarang.cap.service;

import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.AssessmentConfiguration;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.SubmissionReason;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.repository.AssessmentConfigurationRepository;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Enforces max_duration_minutes and marks attempts EXPIRED when policy is violated.
 */
@Component
public class AttemptExpiryGuard {

    private final AssessmentConfigurationRepository configurationRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final AttemptStatusHistoryService statusHistoryService;

    public AttemptExpiryGuard(
            AssessmentConfigurationRepository configurationRepository,
            AssessmentAttemptRepository attemptRepository,
            AttemptStatusHistoryService statusHistoryService
    ) {
        this.configurationRepository = configurationRepository;
        this.attemptRepository = attemptRepository;
        this.statusHistoryService = statusHistoryService;
    }

    public Optional<AssessmentConfiguration> configurationFor(AssessmentAttempt attempt) {
        if (attempt.getAssessmentVersion() == null) {
            return Optional.empty();
        }
        return configurationRepository.findByAssessmentVersionId(attempt.getAssessmentVersion().getId());
    }

    public int elapsedSeconds(AssessmentAttempt attempt) {
        if (attempt.getStartedAt() == null) {
            return 0;
        }
        return (int) ChronoUnit.SECONDS.between(attempt.getStartedAt(), Instant.now());
    }

    public boolean isOverMaxDuration(AssessmentAttempt attempt, AssessmentConfiguration config) {
        if (config == null || config.getMaxDurationMinutes() == null || config.getMaxDurationMinutes() <= 0) {
            return false;
        }
        return elapsedSeconds(attempt) > config.getMaxDurationMinutes() * 60L;
    }

    /**
     * Throws {@code ATTEMPT_EXPIRED} if the attempt exceeded max duration (when not auto-submitting).
     * No-op for non-IN_PROGRESS attempts.
     */
    public void assertWritable(AssessmentAttempt attempt) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return;
        }
        AssessmentConfiguration config = configurationFor(attempt).orElse(null);
        if (!isOverMaxDuration(attempt, config)) {
            return;
        }
        if (config != null && config.isAutoSubmitEnabled()) {
            return;
        }
        expireAttempt(attempt, SubmissionReason.SESSION_EXPIRED);
        throw new BusinessException("Attempt has exceeded the maximum duration", "ATTEMPT_EXPIRED");
    }

    /**
     * On submit: if over max duration and auto-submit is enabled, returns AUTO_SUBMIT; otherwise expires or allows manual submit.
     */
    public SubmissionReason resolveSubmissionReason(AssessmentAttempt attempt, SubmissionReason requested) {
        AssessmentConfiguration config = configurationFor(attempt).orElse(null);
        if (!isOverMaxDuration(attempt, config)) {
            return requested != null ? requested : SubmissionReason.MANUAL_SUBMIT;
        }
        if (config != null && config.isAutoSubmitEnabled()) {
            return SubmissionReason.AUTO_SUBMIT;
        }
        expireAttempt(attempt, SubmissionReason.SESSION_EXPIRED);
        throw new BusinessException("Attempt has exceeded the maximum duration", "ATTEMPT_EXPIRED");
    }

    public void expireAttempt(AssessmentAttempt attempt, SubmissionReason reason) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return;
        }
        AttemptStatus oldStatus = attempt.getStatus();
        attempt.setStatus(AttemptStatus.EXPIRED);
        attempt.setSubmittedAt(Instant.now());
        attempt.setSubmissionReason(reason);
        attempt.setElapsedSeconds(elapsedSeconds(attempt));
        statusHistoryService.record(attempt, oldStatus, AttemptStatus.EXPIRED, "Session expired");
        attemptRepository.save(attempt);
    }

}
