package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.Assessment;
import com.antarang.cap.domain.enums.AssessmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    Optional<Assessment> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCodeAndIsDeletedFalse(UUID tenantId, AssessmentType code);
    Page<Assessment> findByTenantIdAndIsDeletedFalse(UUID tenantId, Pageable pageable);
    Page<Assessment> findByTenantIdAndCodeAndIsDeletedFalse(UUID tenantId, AssessmentType code, Pageable pageable);
}
