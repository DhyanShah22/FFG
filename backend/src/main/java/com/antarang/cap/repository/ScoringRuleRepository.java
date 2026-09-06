package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoringRuleRepository extends JpaRepository<ScoringRule, UUID> {
    List<ScoringRule> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    boolean existsByTenantIdAndCode(UUID tenantId, String code);
    Optional<ScoringRule> findByIdAndTenantId(UUID id, UUID tenantId);
}
