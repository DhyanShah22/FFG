package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ScoringRuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoringRuleVersionRepository extends JpaRepository<ScoringRuleVersion, UUID> {
    List<ScoringRuleVersion> findByScoringRuleIdOrderByVersionNumberAsc(UUID scoringRuleId);
    Optional<ScoringRuleVersion> findByIdAndScoringRuleTenantId(UUID id, UUID tenantId);
    Optional<ScoringRuleVersion> findTopByScoringRuleIdOrderByVersionNumberDesc(UUID scoringRuleId);
}
