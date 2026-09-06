package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.UserRole;
import com.antarang.cap.domain.enums.ScopeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserIdAndIsDeletedFalse(UUID userId);

    Optional<UserRole> findByUserIdAndRoleIdAndIsDeletedFalse(UUID userId, UUID roleId);

    List<UserRole> findByUserIdAndRoleIdAndScopeTypeAndScopeIdAndIsDeletedFalse(
            UUID userId, UUID roleId, ScopeType scopeType, UUID scopeId);
}
