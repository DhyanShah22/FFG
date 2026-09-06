package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentQuestionTiming;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentQuestionTimingRepository extends JpaRepository<AssessmentQuestionTiming, UUID> {
    List<AssessmentQuestionTiming> findByAttemptId(UUID attemptId);
    Optional<AssessmentQuestionTiming> findByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);
}
