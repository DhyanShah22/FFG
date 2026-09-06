package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.IntegrationEvent;
import com.antarang.cap.domain.enums.IntegrationEventStatus;
import com.antarang.cap.domain.enums.IntegrationEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface IntegrationEventRepository extends JpaRepository<IntegrationEvent, UUID>, JpaSpecificationExecutor<IntegrationEvent> {
    Page<IntegrationEvent> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    Page<IntegrationEvent> findByTenantIdAndIntegrationClientIdOrderByCreatedAtDesc(
            UUID tenantId, UUID integrationClientId, Pageable pageable);

    Page<IntegrationEvent> findByTenantIdAndEventTypeOrderByCreatedAtDesc(
            UUID tenantId, IntegrationEventType eventType, Pageable pageable);

    Page<IntegrationEvent> findByTenantIdAndStatusOrderByCreatedAtDesc(
            UUID tenantId, IntegrationEventStatus status, Pageable pageable);
}
