package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentResponseOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssessmentResponseOptionRepository extends JpaRepository<AssessmentResponseOption, UUID> {
    List<AssessmentResponseOption> findByResponseId(UUID responseId);
    void deleteByResponseId(UUID responseId);
}
