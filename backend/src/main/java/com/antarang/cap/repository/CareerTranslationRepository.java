package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.CareerTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareerTranslationRepository extends JpaRepository<CareerTranslation, UUID> {

    List<CareerTranslation> findByCareerId(UUID careerId);

    Optional<CareerTranslation> findByCareerIdAndLanguageId(UUID careerId, UUID languageId);
}
