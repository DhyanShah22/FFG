package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssessmentConfigurationRepository extends JpaRepository<AssessmentConfiguration, UUID> {
    Optional<AssessmentConfiguration> findByAssessmentVersionId(UUID assessmentVersionId);
}
