package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.CareerMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareerMappingRepository extends JpaRepository<CareerMapping, UUID> {
    List<CareerMapping> findByTenantId(UUID tenantId);
    List<CareerMapping> findByTenantIdAndIsActiveTrue(UUID tenantId);
    List<CareerMapping> findByTenantIdAndCareerId(UUID tenantId, UUID careerId);
    Optional<CareerMapping> findByIdAndTenantId(UUID id, UUID tenantId);
}
