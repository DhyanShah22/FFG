package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AttemptStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttemptStatusHistoryRepository extends JpaRepository<AttemptStatusHistory, UUID> {
    List<AttemptStatusHistory> findByAttemptIdOrderByChangedAtAsc(UUID attemptId);
}
