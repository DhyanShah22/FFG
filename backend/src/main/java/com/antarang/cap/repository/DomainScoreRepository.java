package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.DomainScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DomainScoreRepository extends JpaRepository<DomainScore, UUID> {
    List<DomainScore> findByAttemptId(UUID attemptId);
    List<DomainScore> findByStudentId(UUID studentId);
}
