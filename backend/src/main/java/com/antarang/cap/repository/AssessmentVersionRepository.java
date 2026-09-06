package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentVersionRepository extends JpaRepository<AssessmentVersion, UUID> {
    List<AssessmentVersion> findByAssessmentIdOrderByVersionNumberAsc(UUID assessmentId);
    Optional<AssessmentVersion> findByIdAndAssessmentTenantId(UUID id, UUID tenantId);
    Optional<AssessmentVersion> findTopByAssessmentIdOrderByVersionNumberDesc(UUID assessmentId);
}
