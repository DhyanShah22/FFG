package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.Question;
import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.QuestionType;
import com.antarang.cap.domain.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    Optional<Question> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);
    boolean existsByTenantIdAndQuestionCodeAndIsDeletedFalse(UUID tenantId, String questionCode);

    @Query("""
            SELECT q FROM Question q
            WHERE q.tenant.id = :tenantId AND q.isDeleted = false
              AND (:assessmentType IS NULL OR q.assessmentType = :assessmentType)
              AND (:categoryId IS NULL OR q.category.id = :categoryId)
              AND (:questionType IS NULL OR q.questionType = :questionType)
              AND (:reviewStatus IS NULL OR q.reviewStatus = :reviewStatus)
              AND (:isActive IS NULL OR q.isActive = :isActive)
            """)
    Page<Question> search(
            @Param("tenantId") UUID tenantId,
            @Param("assessmentType") AssessmentType assessmentType,
            @Param("categoryId") UUID categoryId,
            @Param("questionType") QuestionType questionType,
            @Param("reviewStatus") ReviewStatus reviewStatus,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("""
            SELECT q FROM Question q
            WHERE q.tenant.id = :tenantId AND q.isDeleted = false
              AND (:assessmentType IS NULL OR q.assessmentType = :assessmentType)
              AND (:categoryId IS NULL OR q.category.id = :categoryId)
              AND (:questionType IS NULL OR q.questionType = :questionType)
              AND (:reviewStatus IS NULL OR q.reviewStatus = :reviewStatus)
              AND (:isActive IS NULL OR q.isActive = :isActive)
              AND (LOWER(q.questionCode) LIKE :searchPattern
                   OR LOWER(q.defaultText) LIKE :searchPattern)
            """)
    Page<Question> searchWithText(
            @Param("tenantId") UUID tenantId,
            @Param("assessmentType") AssessmentType assessmentType,
            @Param("categoryId") UUID categoryId,
            @Param("questionType") QuestionType questionType,
            @Param("reviewStatus") ReviewStatus reviewStatus,
            @Param("isActive") Boolean isActive,
            @Param("searchPattern") String searchPattern,
            Pageable pageable
    );
}
