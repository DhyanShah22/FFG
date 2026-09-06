package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.Questionnaire;
import com.antarang.cap.domain.enums.AssessmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, UUID> {
    Optional<Questionnaire> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCodeAndIsDeletedFalse(UUID tenantId, String code);
    Page<Questionnaire> findByTenantIdAndIsDeletedFalse(UUID tenantId, Pageable pageable);
    Page<Questionnaire> findByTenantIdAndAssessmentTypeAndIsDeletedFalse(UUID tenantId, AssessmentType assessmentType, Pageable pageable);
}
