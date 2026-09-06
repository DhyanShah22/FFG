package com.antarang.cap.service;

import com.antarang.cap.domain.entity.FacilitatorStudentAssignment;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.RecommendationRunStatus;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.dto.response.AdminDashboardResponse;
import com.antarang.cap.dto.response.FacilitatorDashboardResponse;
import com.antarang.cap.dto.response.StudentDashboardResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import com.antarang.cap.repository.AssessmentConfigurationGroupRepository;
import com.antarang.cap.repository.AssessmentRepository;
import com.antarang.cap.repository.FacilitatorStudentAssignmentRepository;
import com.antarang.cap.repository.GeneratedReportRepository;
import com.antarang.cap.repository.RecommendationRunRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final GeneratedReportRepository generatedReportRepository;
    private final RecommendationRunRepository recommendationRunRepository;
    private final FacilitatorStudentAssignmentRepository assignmentRepository;
    private final AssessmentConfigurationGroupRepository configGroupRepository;
    private final UserRepository userRepository;
    private final SubAdminScopeService subAdminScopeService;

    public DashboardService(
            AssessmentRepository assessmentRepository,
            AssessmentAttemptRepository attemptRepository,
            GeneratedReportRepository generatedReportRepository,
            RecommendationRunRepository recommendationRunRepository,
            FacilitatorStudentAssignmentRepository assignmentRepository,
            AssessmentConfigurationGroupRepository configGroupRepository,
            UserRepository userRepository,
            SubAdminScopeService subAdminScopeService
    ) {
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.generatedReportRepository = generatedReportRepository;
        this.recommendationRunRepository = recommendationRunRepository;
        this.assignmentRepository = assignmentRepository;
        this.configGroupRepository = configGroupRepository;
        this.userRepository = userRepository;
        this.subAdminScopeService = subAdminScopeService;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard(UUID studentId) {
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        if (!principal.getId().equals(studentId) && !principal.hasRole(RoleName.ADMINISTRATOR)) {
            throw new BusinessException("Access denied", "ACCESS_DENIED");
        }

        UUID tenantId = principal.getTenantId();
        int assessmentsAvailable = assessmentRepository
                .findByTenantIdAndIsDeletedFalse(tenantId, PageRequest.of(0, 1))
                .getTotalElements() > 0
                ? (int) assessmentRepository.findByTenantIdAndIsDeletedFalse(tenantId, PageRequest.of(0, 1000)).getTotalElements()
                : 0;
        int inProgress = (int) attemptRepository.countByStudentIdAndStatus(studentId, AttemptStatus.IN_PROGRESS);
        int submitted = (int) attemptRepository.countByStudentIdAndStatus(studentId, AttemptStatus.SUBMITTED);
        int reportsReady = (int) generatedReportRepository.countByStudentId(studentId);
        boolean recommendationsReady = recommendationRunRepository
                .findTopByStudentIdAndStatusOrderByGeneratedAtDesc(studentId, RecommendationRunStatus.COMPLETED)
                .isPresent();

        return new StudentDashboardResponse(assessmentsAvailable, inProgress, submitted, reportsReady, recommendationsReady);
    }

    @Transactional(readOnly = true)
    public FacilitatorDashboardResponse getFacilitatorDashboard(UUID facilitatorId) {
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        if (!principal.getId().equals(facilitatorId) && !principal.hasRole(RoleName.ADMINISTRATOR)) {
            throw new BusinessException("Access denied", "ACCESS_DENIED");
        }

        List<FacilitatorStudentAssignment> assignments = assignmentRepository
                .findByFacilitatorIdAndIsActiveTrue(facilitatorId);

        int assignedStudents = assignments.size();
        int submittedCount = 0;
        int inProgressCount = 0;
        int reportsGenerated = 0;

        for (FacilitatorStudentAssignment assignment : assignments) {
            UUID studentId = assignment.getStudent().getId();
            submittedCount += (int) attemptRepository.countByStudentIdAndStatus(studentId, AttemptStatus.SUBMITTED);
            inProgressCount += (int) attemptRepository.countByStudentIdAndStatus(studentId, AttemptStatus.IN_PROGRESS);
            reportsGenerated += (int) generatedReportRepository.countByStudentId(studentId);
        }

        return new FacilitatorDashboardResponse(assignedStudents, submittedCount, inProgressCount, reportsGenerated);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard(UUID orgUnitId, UUID clusterId, Instant fromDate, Instant toDate) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UserPrincipal principal = SecurityUtils.requirePrincipal();

        final Set<UUID> scopedStudentIds;
        if (principal.hasRole(RoleName.SUB_ADMIN)) {
            scopedStudentIds = subAdminScopeService.resolveScopedStudentIds(principal);
        } else if (orgUnitId != null) {
            scopedStudentIds = userRepository.findByTenantIdAndIsDeletedFalse(tenantId).stream()
                    .filter(u -> u.getPrimaryOrgUnit() != null && orgUnitId.equals(u.getPrimaryOrgUnit().getId()))
                    .map(User::getId)
                    .collect(Collectors.toSet());
        } else {
            scopedStudentIds = null;
        }

        long usersActive = userRepository.findByTenantIdAndIsDeletedFalse(tenantId).stream()
                .filter(User::isActive)
                .filter(u -> scopedStudentIds == null || scopedStudentIds.contains(u.getId()))
                .count();

        long attemptsSubmitted = countSubmittedAttempts(tenantId, scopedStudentIds, fromDate, toDate);
        long reportsGenerated = countReports(tenantId, scopedStudentIds, fromDate, toDate);
        long configGroupsActive = configGroupRepository
                .findByTenantIdAndStatusAndIsDeletedFalse(tenantId, LifecycleStatus.ACTIVE, PageRequest.of(0, 1))
                .getTotalElements();

        return new AdminDashboardResponse(usersActive, attemptsSubmitted, reportsGenerated, configGroupsActive);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getSubAdminDashboard(UUID orgUnitId, UUID clusterId, Instant fromDate, Instant toDate) {
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        if (!principal.hasRole(RoleName.SUB_ADMIN)) {
            throw new BusinessException("Access denied", "ACCESS_DENIED");
        }
        return getAdminDashboard(orgUnitId, clusterId, fromDate, toDate);
    }

    private long countSubmittedAttempts(UUID tenantId, Set<UUID> scopedStudentIds, Instant fromDate, Instant toDate) {
        return attemptRepository.findAll().stream()
                .filter(a -> a.getTenant().getId().equals(tenantId))
                .filter(a -> a.getStatus() == AttemptStatus.SUBMITTED)
                .filter(a -> scopedStudentIds == null || scopedStudentIds.contains(a.getStudent().getId()))
                .filter(a -> fromDate == null || (a.getSubmittedAt() != null && !a.getSubmittedAt().isBefore(fromDate)))
                .filter(a -> toDate == null || (a.getSubmittedAt() != null && !a.getSubmittedAt().isAfter(toDate)))
                .count();
    }

    private long countReports(UUID tenantId, Set<UUID> scopedStudentIds, Instant fromDate, Instant toDate) {
        return generatedReportRepository.findAll().stream()
                .filter(r -> r.getStudent().getTenant().getId().equals(tenantId))
                .filter(r -> scopedStudentIds == null || scopedStudentIds.contains(r.getStudent().getId()))
                .filter(r -> fromDate == null || !r.getGeneratedAt().isBefore(fromDate))
                .filter(r -> toDate == null || !r.getGeneratedAt().isAfter(toDate))
                .count();
    }
}
