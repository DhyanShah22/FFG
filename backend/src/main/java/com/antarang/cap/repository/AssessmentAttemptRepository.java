package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.enums.AttemptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {
    List<AssessmentAttempt> findByStudentIdAndAssessmentIdAndStatus(UUID studentId, UUID assessmentId, AttemptStatus status);
    long countByStudentIdAndAssessmentId(UUID studentId, UUID assessmentId);
    long countByStudentIdAndAssessmentIdAndStatusIn(UUID studentId, UUID assessmentId, Collection<AttemptStatus> statuses);
    Page<AssessmentAttempt> findByStudentIdOrderByStartedAtDesc(UUID studentId, Pageable pageable);
    Page<AssessmentAttempt> findByStudentIdAndAssessmentIdOrderByStartedAtDesc(UUID studentId, UUID assessmentId, Pageable pageable);
    Optional<AssessmentAttempt> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<AssessmentAttempt> findTopByStudentIdAndAssessmentIdAndStatusOrderBySubmittedAtDesc(
            UUID studentId, UUID assessmentId, AttemptStatus status);
    Optional<AssessmentAttempt> findTopByStudentIdAndStatusOrderBySubmittedAtDesc(
            UUID studentId, AttemptStatus status);
    long countByStudentIdAndStatus(UUID studentId, AttemptStatus status);
}
