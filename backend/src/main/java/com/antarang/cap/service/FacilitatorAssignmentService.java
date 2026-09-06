package com.antarang.cap.service;

import com.antarang.cap.domain.entity.AssignmentHistory;
import com.antarang.cap.domain.entity.FacilitatorStudentAssignment;
import com.antarang.cap.domain.entity.OrgUnit;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.AssignmentAction;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.dto.request.AssignStudentsRequest;
import com.antarang.cap.dto.response.AssignmentHistoryResponse;
import com.antarang.cap.dto.response.FacilitatorStudentResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssignmentHistoryRepository;
import com.antarang.cap.repository.FacilitatorStudentAssignmentRepository;
import com.antarang.cap.repository.OrgUnitRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FacilitatorAssignmentService {

    private final FacilitatorStudentAssignmentRepository assignmentRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final UserRepository userRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final AuditService auditService;

    public FacilitatorAssignmentService(
            FacilitatorStudentAssignmentRepository assignmentRepository,
            AssignmentHistoryRepository assignmentHistoryRepository,
            UserRepository userRepository,
            OrgUnitRepository orgUnitRepository,
            AuditService auditService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.userRepository = userRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.auditService = auditService;
    }

    @Transactional
    public List<FacilitatorStudentResponse> assignStudents(UUID facilitatorId, AssignStudentsRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        User facilitator = getTenantUser(principal, facilitatorId);
        if (facilitator.getProfileType() != ProfileType.CAREER_COUNSELLOR) {
            throw new BusinessException("Target user is not a facilitator", "VALIDATION_ERROR");
        }

        OrgUnit orgUnit = null;
        if (request.orgUnitId() != null) {
            orgUnit = orgUnitRepository.findById(request.orgUnitId())
                    .filter(found -> !found.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Org unit not found"));
            if (!orgUnit.getTenant().getId().equals(principal.getTenantId())) {
                throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
            }
        }

        List<FacilitatorStudentResponse> results = new ArrayList<>();
        for (UUID studentId : request.studentIds()) {
            User student = getTenantUser(principal, studentId);
            if (student.getProfileType() != ProfileType.CAREER_EXPLORER) {
                throw new BusinessException("Target user is not a student: " + studentId, "VALIDATION_ERROR");
            }

            UUID oldFacilitatorId = null;
            var existingActive = assignmentRepository.findByStudentIdAndIsActiveTrue(studentId);
            for (FacilitatorStudentAssignment existing : existingActive) {
                if (!existing.getFacilitator().getId().equals(facilitatorId)) {
                    oldFacilitatorId = existing.getFacilitator().getId();
                    existing.setActive(false);
                    existing.setUnassignedAt(Instant.now());
                    assignmentRepository.save(existing);
                }
            }

            FacilitatorStudentAssignment assignment = assignmentRepository
                    .findByFacilitatorIdAndStudentIdAndIsActiveTrue(facilitatorId, studentId)
                    .orElseGet(() -> {
                        FacilitatorStudentAssignment created = new FacilitatorStudentAssignment();
                        created.setTenant(facilitator.getTenant());
                        created.setFacilitator(facilitator);
                        created.setStudent(student);
                        return created;
                    });
            assignment.setOrgUnit(orgUnit);
            assignment.setAssignedBy(principal.getId());
            assignment.setActive(true);
            assignment.setAssignedAt(Instant.now());
            assignment.setUnassignedAt(null);
            FacilitatorStudentAssignment saved = assignmentRepository.save(assignment);

            AssignmentHistory history = new AssignmentHistory();
            history.setAssignment(saved);
            history.setStudent(student);
            history.setOldFacilitatorId(oldFacilitatorId);
            history.setNewFacilitatorId(facilitatorId);
            history.setAction(oldFacilitatorId != null ? AssignmentAction.REASSIGNED : AssignmentAction.ASSIGNED);
            history.setPerformedBy(principal.getId());
            assignmentHistoryRepository.save(history);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("studentId", studentId.toString());
            newValue.put("facilitatorId", facilitatorId.toString());
            newValue.put("action", history.getAction().name());
            auditService.record("facilitator_student_assignment", saved.getId(), AuditAction.ASSIGN, null, newValue);

            results.add(toResponse(saved));
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<FacilitatorStudentResponse> listStudents(UUID facilitatorId) {
        UserPrincipal principal = getCurrentPrincipal();
        getTenantUser(principal, facilitatorId);
        return assignmentRepository.findByFacilitatorIdAndIsActiveTrue(facilitatorId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void unassignStudent(UUID facilitatorId, UUID studentId) {
        UserPrincipal principal = getCurrentPrincipal();
        getTenantUser(principal, facilitatorId);
        FacilitatorStudentAssignment assignment = assignmentRepository
                .findByFacilitatorIdAndStudentIdAndIsActiveTrue(facilitatorId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active assignment not found"));

        assignment.setActive(false);
        assignment.setUnassignedAt(Instant.now());
        FacilitatorStudentAssignment saved = assignmentRepository.save(assignment);

        AssignmentHistory history = new AssignmentHistory();
        history.setAssignment(saved);
        history.setStudent(saved.getStudent());
        history.setOldFacilitatorId(facilitatorId);
        history.setNewFacilitatorId(null);
        history.setAction(AssignmentAction.UNASSIGNED);
        history.setPerformedBy(principal.getId());
        assignmentHistoryRepository.save(history);

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("studentId", studentId.toString());
        oldValue.put("facilitatorId", facilitatorId.toString());
        oldValue.put("action", AssignmentAction.UNASSIGNED.name());
        auditService.record("facilitator_student_assignment", saved.getId(), AuditAction.DELETE, oldValue, null);
    }

    @Transactional(readOnly = true)
    public List<AssignmentHistoryResponse> assignmentHistory(UUID studentId) {
        UserPrincipal principal = getCurrentPrincipal();
        getTenantUser(principal, studentId);
        return assignmentHistoryRepository.findByStudentIdOrderByPerformedAtDesc(studentId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private User getTenantUser(UserPrincipal principal, UUID userId) {
        User user = userRepository.findById(userId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }
        return user;
    }

    private UserPrincipal getCurrentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authentication required", "UNAUTHORIZED");
        }
        return principal;
    }

    private FacilitatorStudentResponse toResponse(FacilitatorStudentAssignment assignment) {
        return new FacilitatorStudentResponse(
                assignment.getId(),
                assignment.getFacilitator().getId(),
                assignment.getStudent().getId(),
                assignment.getOrgUnit() != null ? assignment.getOrgUnit().getId() : null,
                assignment.isActive(),
                assignment.getAssignedAt(),
                assignment.getUnassignedAt()
        );
    }

    private AssignmentHistoryResponse toHistoryResponse(AssignmentHistory history) {
        return new AssignmentHistoryResponse(
                history.getId(),
                history.getAssignment().getId(),
                history.getStudent().getId(),
                history.getOldFacilitatorId(),
                history.getNewFacilitatorId(),
                history.getAction(),
                history.getPerformedBy(),
                history.getPerformedAt(),
                history.getRemarks()
        );
    }
}
