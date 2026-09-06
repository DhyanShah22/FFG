package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    List<Recommendation> findByRecommendationRunIdOrderByRankOrderAsc(UUID recommendationRunId);
    Page<Recommendation> findByRecommendationRunIdOrderByRankOrderAsc(UUID recommendationRunId, Pageable pageable);
}
