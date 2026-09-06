package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentConfigurationGroupItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentConfigurationGroupItemRepository extends JpaRepository<AssessmentConfigurationGroupItem, UUID> {
    List<AssessmentConfigurationGroupItem> findByConfigurationGroupIdOrderByDisplayOrderAsc(UUID groupId);
    Optional<AssessmentConfigurationGroupItem> findByConfigurationGroupIdAndAssessmentId(UUID groupId, UUID assessmentId);
}
