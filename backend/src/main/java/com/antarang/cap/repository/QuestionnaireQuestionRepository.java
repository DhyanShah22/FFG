package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.QuestionnaireQuestion;
import com.antarang.cap.domain.enums.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestionnaireQuestionRepository extends JpaRepository<QuestionnaireQuestion, UUID> {
    List<QuestionnaireQuestion> findByQuestionnaireVersionIdOrderByDisplayOrderAsc(UUID versionId);
    boolean existsByQuestionIdAndQuestionnaireVersionStatus(UUID questionId, VersionStatus status);

    @Query("""
            SELECT COUNT(qq) > 0 FROM QuestionnaireQuestion qq
            WHERE qq.question.id = :questionId AND qq.questionnaireVersion.status = :status
            """)
    boolean isQuestionOnVersionWithStatus(@Param("questionId") UUID questionId, @Param("status") VersionStatus status);

    void deleteByQuestionnaireVersionId(UUID versionId);
}
