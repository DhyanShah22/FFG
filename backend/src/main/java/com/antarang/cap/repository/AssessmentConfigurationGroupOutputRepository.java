package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentConfigurationGroupOutput;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssessmentConfigurationGroupOutputRepository extends JpaRepository<AssessmentConfigurationGroupOutput, UUID> {
    List<AssessmentConfigurationGroupOutput> findByConfigurationGroupIdAndIsActiveTrue(UUID groupId);
}
