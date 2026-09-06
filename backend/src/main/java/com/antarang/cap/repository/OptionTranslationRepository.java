package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.OptionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OptionTranslationRepository extends JpaRepository<OptionTranslation, UUID> {
    List<OptionTranslation> findByOptionId(UUID optionId);
    List<OptionTranslation> findByOptionQuestionId(UUID questionId);
    Optional<OptionTranslation> findByOptionIdAndLanguageId(UUID optionId, UUID languageId);
}
