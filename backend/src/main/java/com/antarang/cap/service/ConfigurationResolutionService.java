package com.antarang.cap.service;

import com.antarang.cap.domain.entity.AssessmentConfigurationGroup;
import com.antarang.cap.domain.entity.AssessmentConfigurationGroupAssignment;
import com.antarang.cap.domain.entity.AssessmentConfigurationGroupItem;
import com.antarang.cap.domain.entity.OrganizationalClusterMember;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.AssignmentType;
import com.antarang.cap.domain.enums.ClusterMemberType;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentConfigurationGroupAssignmentRepository;
import com.antarang.cap.repository.AssessmentConfigurationGroupItemRepository;
import com.antarang.cap.repository.OrganizationalClusterMemberRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConfigurationResolutionService {

    private final UserRepository userRepository;
    private final AssessmentConfigurationGroupAssignmentRepository assignmentRepository;
    private final AssessmentConfigurationGroupItemRepository itemRepository;
    private final OrganizationalClusterMemberRepository clusterMemberRepository;

    public ConfigurationResolutionService(
            UserRepository userRepository,
            AssessmentConfigurationGroupAssignmentRepository assignmentRepository,
            AssessmentConfigurationGroupItemRepository itemRepository,
            OrganizationalClusterMemberRepository clusterMemberRepository
    ) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.itemRepository = itemRepository;
        this.clusterMemberRepository = clusterMemberRepository;
    }

    public record ResolutionResult(
            AssessmentConfigurationGroup group,
            AssessmentConfigurationGroupAssignment assignment,
            AssessmentConfigurationGroupItem matchedItem,
            List<AssessmentConfigurationGroupItem> items
    ) {
    }

    @Transactional(readOnly = true)
    public ResolutionResult resolve(UUID studentId, UUID assessmentId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        User student = userRepository.findById(studentId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        SecurityUtils.assertSameTenant(student.getTenant().getId());

        Optional<AssessmentConfigurationGroupAssignment> userAssignment = firstActiveAssignment(
                tenantId, AssignmentType.USER, studentId);
        if (userAssignment.isPresent()) {
            return toResult(userAssignment.get(), assessmentId);
        }

        List<OrganizationalClusterMember> memberships = clusterMemberRepository
                .findByMemberTypeAndMemberIdAndIsActiveTrue(ClusterMemberType.USER, studentId);
        for (OrganizationalClusterMember membership : memberships) {
            Optional<AssessmentConfigurationGroupAssignment> clusterAssignment = firstActiveAssignment(
                    tenantId, AssignmentType.CLUSTER, membership.getCluster().getId());
            if (clusterAssignment.isPresent()) {
                return toResult(clusterAssignment.get(), assessmentId);
            }
        }

        if (student.getPrimaryOrgUnit() != null) {
            Optional<AssessmentConfigurationGroupAssignment> orgAssignment = firstActiveAssignment(
                    tenantId, AssignmentType.ORG_UNIT, student.getPrimaryOrgUnit().getId());
            if (orgAssignment.isPresent()) {
                return toResult(orgAssignment.get(), assessmentId);
            }
        }

        throw new ResourceNotFoundException("No active assessment configuration found for student");
    }

    private Optional<AssessmentConfigurationGroupAssignment> firstActiveAssignment(
            UUID tenantId,
            AssignmentType type,
            UUID assignmentId
    ) {
        List<AssessmentConfigurationGroupAssignment> assignments = assignmentRepository.findActiveAssignments(
                tenantId, LifecycleStatus.ACTIVE, type, assignmentId);
        return assignments.stream().findFirst();
    }

    private ResolutionResult toResult(AssessmentConfigurationGroupAssignment assignment, UUID assessmentId) {
        AssessmentConfigurationGroup group = assignment.getConfigurationGroup();
        AssessmentConfigurationGroupItem matchedItem = null;
        List<AssessmentConfigurationGroupItem> items = List.of();
        if (assessmentId != null) {
            matchedItem = itemRepository.findByConfigurationGroupIdAndAssessmentId(group.getId(), assessmentId)
                    .orElse(null);
        } else {
            items = itemRepository.findByConfigurationGroupIdOrderByDisplayOrderAsc(group.getId());
        }
        return new ResolutionResult(group, assignment, matchedItem, items);
    }
}
