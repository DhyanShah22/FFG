package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.CareerCluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareerClusterRepository extends JpaRepository<CareerCluster, UUID> {
    List<CareerCluster> findByTenantIdOrderByNameAsc(UUID tenantId);
    boolean existsByTenantIdAndCode(UUID tenantId, String code);
    Optional<CareerCluster> findByIdAndTenantId(UUID id, UUID tenantId);
}
