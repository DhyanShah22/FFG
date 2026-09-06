package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.FacilitatorStudentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FacilitatorStudentAssignmentRepository extends JpaRepository<FacilitatorStudentAssignment, UUID> {

    List<FacilitatorStudentAssignment> findByFacilitatorIdAndIsActiveTrue(UUID facilitatorId);

    List<FacilitatorStudentAssignment> findByStudentIdAndIsActiveTrue(UUID studentId);

    Optional<FacilitatorStudentAssignment> findByFacilitatorIdAndStudentIdAndIsActiveTrue(
            UUID facilitatorId, UUID studentId);

    List<FacilitatorStudentAssignment> findByTenantIdAndIsActiveTrue(UUID tenantId);
}
