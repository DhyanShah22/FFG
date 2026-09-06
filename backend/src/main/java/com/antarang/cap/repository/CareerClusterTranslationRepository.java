package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.CareerClusterTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareerClusterTranslationRepository extends JpaRepository<CareerClusterTranslation, UUID> {

    List<CareerClusterTranslation> findByCareerClusterId(UUID careerClusterId);

    Optional<CareerClusterTranslation> findByCareerClusterIdAndLanguageId(UUID careerClusterId, UUID languageId);
}
