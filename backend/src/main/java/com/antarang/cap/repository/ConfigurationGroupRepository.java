package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.ConfigurationGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfigurationGroupRepository extends JpaRepository<ConfigurationGroup, UUID> {

    Optional<ConfigurationGroup> findByCodeAndIsDeletedFalse(String code);

    Optional<ConfigurationGroup> findByTenantIdAndCodeAndIsDeletedFalse(UUID tenantId, String code);

    List<ConfigurationGroup> findByTenantIdAndIsDeletedFalse(UUID tenantId);

    List<ConfigurationGroup> findByIsSystemDefinedTrueAndIsDeletedFalse();

    List<ConfigurationGroup> findByIsDeletedFalse();
}
