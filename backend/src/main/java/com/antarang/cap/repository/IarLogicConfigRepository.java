package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.IarLogicConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IarLogicConfigRepository extends JpaRepository<IarLogicConfig, UUID> {
    Optional<IarLogicConfig> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCode(UUID tenantId, String code);
    List<IarLogicConfig> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
