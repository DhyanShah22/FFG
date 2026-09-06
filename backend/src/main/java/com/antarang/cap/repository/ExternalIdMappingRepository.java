package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ExternalIdMapping;
import com.antarang.cap.domain.enums.ExternalEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExternalIdMappingRepository extends JpaRepository<ExternalIdMapping, UUID> {
    Optional<ExternalIdMapping> findByExternalSystemAndExternalEntityTypeAndExternalEntityId(
            String externalSystem, ExternalEntityType externalEntityType, String externalEntityId);

    Optional<ExternalIdMapping> findByExternalSystemAndExternalEntityTypeAndExternalEntityIdAndTenantId(
            String externalSystem, ExternalEntityType externalEntityType, String externalEntityId, UUID tenantId);
}
