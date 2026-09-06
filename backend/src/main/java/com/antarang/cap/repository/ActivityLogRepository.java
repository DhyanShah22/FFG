package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ActivityLog;
import com.antarang.cap.domain.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID>, JpaSpecificationExecutor<ActivityLog> {
    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<ActivityLog> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);
    Page<ActivityLog> findByTenantIdAndActivityTypeOrderByCreatedAtDesc(UUID tenantId, ActivityType activityType, Pageable pageable);
}
