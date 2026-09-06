package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentResponseRepository extends JpaRepository<AssessmentResponse, UUID> {
    List<AssessmentResponse> findByAttemptId(UUID attemptId);
    Optional<AssessmentResponse> findByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);
}
