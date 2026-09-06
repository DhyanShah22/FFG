package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.QuestionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionTranslationRepository extends JpaRepository<QuestionTranslation, UUID> {
    List<QuestionTranslation> findByQuestionId(UUID questionId);
    Optional<QuestionTranslation> findByQuestionIdAndLanguageId(UUID questionId, UUID languageId);
}
