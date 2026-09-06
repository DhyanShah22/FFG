package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.OrganizationalCluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationalClusterRepository extends JpaRepository<OrganizationalCluster, UUID> {

    Optional<OrganizationalCluster> findByTenantIdAndCodeAndIsDeletedFalse(UUID tenantId, String code);

    List<OrganizationalCluster> findByTenantIdAndIsDeletedFalse(UUID tenantId);
}
