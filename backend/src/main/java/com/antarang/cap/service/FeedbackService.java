package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.CounsellorFeedback;
import com.antarang.cap.domain.entity.GeneratedReport;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.FeedbackVisibility;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.dto.request.CreateFeedbackRequest;
import com.antarang.cap.dto.request.UpdateFeedbackRequest;
import com.antarang.cap.dto.response.FeedbackResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.CounsellorFeedbackRepository;
import com.antarang.cap.repository.FacilitatorStudentAssignmentRepository;
import com.antarang.cap.repository.GeneratedReportRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.StudentAccessGuard;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FeedbackService {

    private final CounsellorFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final GeneratedReportRepository generatedReportRepository;
    private final FacilitatorStudentAssignmentRepository assignmentRepository;
    private final StudentAccessGuard studentAccessGuard;
    private final AuditService auditService;

    public FeedbackService(
            CounsellorFeedbackRepository feedbackRepository,
            UserRepository userRepository,
            GeneratedReportRepository generatedReportRepository,
            FacilitatorStudentAssignmentRepository assignmentRepository,
            StudentAccessGuard studentAccessGuard,
            AuditService auditService
    ) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.generatedReportRepository = generatedReportRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentAccessGuard = studentAccessGuard;
        this.auditService = auditService;
    }

    @Transactional
    public FeedbackResponse create(UUID studentId, CreateFeedbackRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        assertCounsellorOrAdmin(principal, studentId);

        User student = userRepository.findById(studentId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        SecurityUtils.assertSameTenant(student.getTenant().getId());

        User counsellor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Counsellor not found"));

        GeneratedReport report = null;
        if (request.reportId() != null) {
            report = generatedReportRepository.findById(request.reportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
            if (!report.getStudent().getId().equals(studentId)) {
                throw new BusinessException("Report does not belong to student", "ACCESS_DENIED");
            }
        }

        CounsellorFeedback feedback = new CounsellorFeedback();
        feedback.setTenant(student.getTenant());
        feedback.setStudent(student);
        feedback.setCounsellor(counsellor);
        feedback.setReport(report);
        feedback.setFeedbackText(request.feedbackText());
        feedback.setRecommendationNotes(request.recommendationNotes());
        feedback.setVisibility(request.visibility() != null ? request.visibility() : FeedbackVisibility.INTERNAL);

        CounsellorFeedback saved = feedbackRepository.save(feedback);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("studentId", studentId.toString());
        newValue.put("visibility", saved.getVisibility().name());
        auditService.record("counsellor_feedback", saved.getId(), AuditAction.CREATE, null, newValue);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedbackResponse> list(UUID studentId, int page, int size) {
        UserPrincipal principal = SecurityUtils.requirePrincipal();

        if (principal.hasRole(RoleName.CAREER_EXPLORER)) {
            if (!principal.getId().equals(studentId)) {
                throw new BusinessException("Access denied", "ACCESS_DENIED");
            }
        } else {
            studentAccessGuard.assertStudentAccess(studentId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<CounsellorFeedback> feedbackPage;

        if (principal.hasRole(RoleName.CAREER_EXPLORER)) {
            feedbackPage = feedbackRepository.findByStudentIdAndVisibilityAndIsActiveTrueOrderByCreatedAtDesc(
                    studentId, FeedbackVisibility.VISIBLE_TO_STUDENT, pageable);
        } else {
            feedbackPage = feedbackRepository.findByStudentIdAndIsActiveTrueOrderByCreatedAtDesc(studentId, pageable);
        }

        return PageResponse.from(feedbackPage.map(this::toResponse));
    }

    @Transactional
    public FeedbackResponse update(UUID feedbackId, UpdateFeedbackRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UserPrincipal principal = SecurityUtils.requirePrincipal();

        CounsellorFeedback feedback = feedbackRepository.findByIdAndTenantId(feedbackId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));

        if (!principal.hasRole(RoleName.ADMINISTRATOR) && !feedback.getCounsellor().getId().equals(principal.getId())) {
            throw new BusinessException("Only the original counsellor or admin can update feedback", "ACCESS_DENIED");
        }

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("feedbackText", feedback.getFeedbackText());
        oldValue.put("visibility", feedback.getVisibility().name());

        if (request.feedbackText() != null) {
            feedback.setFeedbackText(request.feedbackText());
        }
        if (request.recommendationNotes() != null) {
            feedback.setRecommendationNotes(request.recommendationNotes());
        }
        if (request.visibility() != null) {
            feedback.setVisibility(request.visibility());
        }
        if (request.reportId() != null) {
            GeneratedReport report = generatedReportRepository.findById(request.reportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
            feedback.setReport(report);
        }
        feedback.setUpdatedAt(Instant.now());

        CounsellorFeedback saved = feedbackRepository.save(feedback);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("feedbackText", saved.getFeedbackText());
        newValue.put("visibility", saved.getVisibility().name());
        auditService.record("counsellor_feedback", saved.getId(), AuditAction.UPDATE, oldValue, newValue);

        return toResponse(saved);
    }

    private void assertCounsellorOrAdmin(UserPrincipal principal, UUID studentId) {
        if (principal.hasRole(RoleName.ADMINISTRATOR) || principal.hasRole(RoleName.SUPER_ADMIN)) {
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
        throw new BusinessException("Access denied", "ACCESS_DENIED");
    }

    private FeedbackResponse toResponse(CounsellorFeedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getStudent().getId(),
                feedback.getCounsellor().getId(),
                feedback.getReport() != null ? feedback.getReport().getId() : null,
                feedback.getFeedbackText(),
                feedback.getRecommendationNotes(),
                feedback.getVisibility(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }
}
