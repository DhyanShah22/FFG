package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.NotificationLog;
import com.antarang.cap.domain.enums.NotificationLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID>, JpaSpecificationExecutor<NotificationLog> {
    Page<NotificationLog> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    Page<NotificationLog> findByTenantIdAndUserIdOrderByCreatedAtDesc(UUID tenantId, UUID userId, Pageable pageable);

    Page<NotificationLog> findByTenantIdAndStatusOrderByCreatedAtDesc(
            UUID tenantId, NotificationLogStatus status, Pageable pageable);
}
