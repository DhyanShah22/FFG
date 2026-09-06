package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentConfigurationGroupAssignment;
import com.antarang.cap.domain.enums.AssignmentType;
import com.antarang.cap.domain.enums.LifecycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentConfigurationGroupAssignmentRepository extends JpaRepository<AssessmentConfigurationGroupAssignment, UUID> {
    List<AssessmentConfigurationGroupAssignment> findByConfigurationGroupIdAndIsActiveTrue(UUID groupId);
    Optional<AssessmentConfigurationGroupAssignment> findByIdAndConfigurationGroupId(UUID id, UUID groupId);

    @Query("""
            SELECT a FROM AssessmentConfigurationGroupAssignment a
            JOIN a.configurationGroup g
            WHERE a.isActive = true AND g.isDeleted = false AND g.status = :status AND g.tenant.id = :tenantId
              AND a.assignmentType = :type AND a.assignmentId = :assignmentId
              AND (a.effectiveFrom IS NULL OR a.effectiveFrom <= CURRENT_TIMESTAMP)
              AND (a.effectiveTo IS NULL OR a.effectiveTo >= CURRENT_TIMESTAMP)
            """)
    List<AssessmentConfigurationGroupAssignment> findActiveAssignments(
            @Param("tenantId") UUID tenantId,
            @Param("status") LifecycleStatus status,
            @Param("type") AssignmentType type,
            @Param("assignmentId") UUID assignmentId
    );
}
