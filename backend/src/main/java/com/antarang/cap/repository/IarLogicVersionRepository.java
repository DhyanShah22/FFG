package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.IarLogicVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IarLogicVersionRepository extends JpaRepository<IarLogicVersion, UUID> {
    List<IarLogicVersion> findByIarLogicConfigIdOrderByVersionNumberAsc(UUID configId);
    Optional<IarLogicVersion> findByIdAndIarLogicConfigTenantId(UUID id, UUID tenantId);
    Optional<IarLogicVersion> findTopByIarLogicConfigIdOrderByVersionNumberDesc(UUID configId);
}
