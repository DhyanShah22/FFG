package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.QuestionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRuleRepository extends JpaRepository<QuestionRule, UUID> {
    List<QuestionRule> findByQuestionIdAndIsActiveTrue(UUID questionId);
    Optional<QuestionRule> findByIdAndQuestionId(UUID id, UUID questionId);
}
