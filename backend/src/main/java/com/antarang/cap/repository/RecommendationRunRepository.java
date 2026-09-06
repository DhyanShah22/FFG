package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.RecommendationRun;
import com.antarang.cap.domain.enums.RecommendationRunStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecommendationRunRepository extends JpaRepository<RecommendationRun, UUID> {
    Page<RecommendationRun> findByStudentIdOrderByGeneratedAtDesc(UUID studentId, Pageable pageable);
    Optional<RecommendationRun> findTopByStudentIdAndStatusOrderByGeneratedAtDesc(UUID studentId, RecommendationRunStatus status);
    Optional<RecommendationRun> findByIdAndStudentId(UUID id, UUID studentId);
}
