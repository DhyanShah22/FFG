package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.QuestionnaireVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionnaireVersionRepository extends JpaRepository<QuestionnaireVersion, UUID> {
    List<QuestionnaireVersion> findByQuestionnaireIdOrderByVersionNumberAsc(UUID questionnaireId);
    Optional<QuestionnaireVersion> findByIdAndQuestionnaireTenantId(UUID id, UUID tenantId);
    Optional<QuestionnaireVersion> findTopByQuestionnaireIdOrderByVersionNumberDesc(UUID questionnaireId);
}
