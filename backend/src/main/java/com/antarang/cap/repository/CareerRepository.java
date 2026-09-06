package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareerRepository extends JpaRepository<Career, UUID> {
    List<Career> findByCareerClusterIdOrderByNameAsc(UUID careerClusterId);
    boolean existsByCareerClusterIdAndCode(UUID careerClusterId, String code);
    Optional<Career> findByIdAndCareerClusterTenantId(UUID id, UUID tenantId);
}
