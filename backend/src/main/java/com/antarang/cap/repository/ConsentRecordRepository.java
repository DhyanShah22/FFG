package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ConsentRecord;
import com.antarang.cap.domain.enums.ConsentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {

    List<ConsentRecord> findByUserId(UUID userId);

    Optional<ConsentRecord> findFirstByUserIdAndConsentTypeOrderByCreatedAtDesc(UUID userId, ConsentType consentType);
}
