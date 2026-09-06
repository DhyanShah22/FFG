package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.QuestionCategory;
import com.antarang.cap.domain.enums.AssessmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, UUID> {
    Optional<QuestionCategory> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndAssessmentTypeAndCode(UUID tenantId, AssessmentType assessmentType, String code);
    Page<QuestionCategory> findByTenantIdAndAssessmentType(UUID tenantId, AssessmentType assessmentType, Pageable pageable);
    Page<QuestionCategory> findByTenantId(UUID tenantId, Pageable pageable);
}
