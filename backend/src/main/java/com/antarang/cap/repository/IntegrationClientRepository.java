package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.IntegrationClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationClientRepository extends JpaRepository<IntegrationClient, UUID> {
    boolean existsByTenantIdAndClientCode(UUID tenantId, String clientCode);
    Optional<IntegrationClient> findByIdAndTenantId(UUID id, UUID tenantId);
    List<IntegrationClient> findByIsActiveTrueAndApiKeyHashIsNotNull();
}
