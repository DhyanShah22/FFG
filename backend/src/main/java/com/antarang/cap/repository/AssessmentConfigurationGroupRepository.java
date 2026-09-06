package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssessmentConfigurationGroup;
import com.antarang.cap.domain.enums.LifecycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssessmentConfigurationGroupRepository extends JpaRepository<AssessmentConfigurationGroup, UUID> {
    Optional<AssessmentConfigurationGroup> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCodeAndIsDeletedFalse(UUID tenantId, String code);
    Page<AssessmentConfigurationGroup> findByTenantIdAndIsDeletedFalse(UUID tenantId, Pageable pageable);
    Page<AssessmentConfigurationGroup> findByTenantIdAndStatusAndIsDeletedFalse(UUID tenantId, LifecycleStatus status, Pageable pageable);
    Page<AssessmentConfigurationGroup> findByTenantIdAndAcademicYearAndIsDeletedFalse(UUID tenantId, String academicYear, Pageable pageable);
    Page<AssessmentConfigurationGroup> findByTenantIdAndStatusAndAcademicYearAndIsDeletedFalse(
            UUID tenantId, LifecycleStatus status, String academicYear, Pageable pageable);
}
