package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, UUID> {

    List<OrgUnit> findByTenantIdAndIsDeletedFalse(UUID tenantId);
}
