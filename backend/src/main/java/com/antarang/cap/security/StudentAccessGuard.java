package com.antarang.cap.security;

import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.ClusterMemberType;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.domain.enums.ScopeType;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.repository.FacilitatorStudentAssignmentRepository;
import com.antarang.cap.repository.OrganizationalClusterMemberRepository;
import com.antarang.cap.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StudentAccessGuard {

    private final FacilitatorStudentAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final OrganizationalClusterMemberRepository clusterMemberRepository;

    public StudentAccessGuard(
            FacilitatorStudentAssignmentRepository assignmentRepository,
            UserRepository userRepository,
            OrganizationalClusterMemberRepository clusterMemberRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.clusterMemberRepository = clusterMemberRepository;
    }

    public void assertStudentAccess(UUID studentId) {
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        SecurityUtils.assertSameTenant(principal.getTenantId());

        if (principal.hasRole(RoleName.ADMINISTRATOR) || principal.hasRole(RoleName.SUPER_ADMIN)) {
            return;
        }

        if (principal.hasRole(RoleName.CAREER_EXPLORER)) {
            if (!principal.getId().equals(studentId)) {
                throw new BusinessException("Access denied", "ACCESS_DENIED");
            }
            return;
        }

        if (principal.hasRole(RoleName.CAREER_COUNSELLOR)) {
            boolean assigned = assignmentRepository
                    .findByFacilitatorIdAndStudentIdAndIsActiveTrue(principal.getId(), studentId)
                    .isPresent();
            if (!assigned) {
                throw new BusinessException("Access denied", "ACCESS_DENIED");
            }
            return;
        }

        if (principal.hasRole(RoleName.SUB_ADMIN)) {
            assertSubAdminScope(principal, studentId);
            return;
        }

        throw new BusinessException("Access denied", "ACCESS_DENIED");
    }

    public void assertAttemptOwner(com.antarang.cap.domain.entity.AssessmentAttempt attempt) {
        SecurityUtils.assertSameTenant(attempt.getTenant().getId());
        assertStudentAccess(attempt.getStudent().getId());
    }

    private void assertSubAdminScope(UserPrincipal principal, UUID studentId) {
        ScopeType scopeType = principal.getScopeType();
        UUID scopeId = principal.getScopeId();
        if (scopeType == null || scopeType == ScopeType.GLOBAL || scopeType == ScopeType.TENANT || scopeId == null) {
            return;
        }

        User student = userRepository.findById(studentId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new BusinessException("Student not found", "RESOURCE_NOT_FOUND"));

        if (scopeType == ScopeType.ORG_UNIT) {
            if (student.getPrimaryOrgUnit() == null || !scopeId.equals(student.getPrimaryOrgUnit().getId())) {
                throw new BusinessException("Access denied for student outside sub-admin scope", "ACCESS_DENIED");
            }
            return;
        }

        if (scopeType == ScopeType.CLUSTER) {
            boolean inCluster = clusterMemberRepository
                    .findByMemberTypeAndMemberIdAndIsActiveTrue(ClusterMemberType.USER, studentId)
                    .stream()
                    .anyMatch(membership -> scopeId.equals(membership.getCluster().getId()));
            if (!inCluster) {
                throw new BusinessException("Access denied for student outside sub-admin cluster scope", "ACCESS_DENIED");
            }
        }
    }
}
