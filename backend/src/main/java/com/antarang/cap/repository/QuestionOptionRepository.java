package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {
    List<QuestionOption> findByQuestionIdOrderByDisplayOrderAsc(UUID questionId);
    Optional<QuestionOption> findByIdAndQuestionId(UUID id, UUID questionId);
    boolean existsByQuestionIdAndOptionCode(UUID questionId, String optionCode);
}
