package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByCodeAndIsDeletedFalse(String code);
}
