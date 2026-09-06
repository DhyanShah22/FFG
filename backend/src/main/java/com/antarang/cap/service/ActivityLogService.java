package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.ActivityLog;
import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.GeneratedReport;
import com.antarang.cap.domain.enums.ActivityType;
import com.antarang.cap.dto.response.ActivityLogResponse;
import com.antarang.cap.repository.ActivityLogRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void logAssessmentSubmit(AssessmentAttempt attempt) {
        UUID tenantId = attempt.getTenant().getId();
        UUID userId = attempt.getStudent().getId();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("attemptId", attempt.getId().toString());
        metadata.put("assessmentId", attempt.getAssessment().getId().toString());
        metadata.put("assessmentCode", attempt.getAssessment().getCode());
        metadata.put("status", attempt.getStatus().name());
        metadata.put("scoreStatus", attempt.getScoreStatus().name());
        if (attempt.getSubmittedAt() != null) {
            metadata.put("submittedAt", attempt.getSubmittedAt().toString());
        }
        if (attempt.getElapsedSeconds() != null) {
            metadata.put("elapsedSeconds", attempt.getElapsedSeconds());
        }
        if (attempt.getSubmissionReason() != null) {
            metadata.put("submissionReason", attempt.getSubmissionReason().name());
        }

        ActivityLog log = new ActivityLog();
        log.setTenantId(tenantId);
        log.setUserId(SecurityUtils.requirePrincipal().getId());
        log.setActivityType(ActivityType.ASSESSMENT_SUBMIT);
        log.setDescription("Assessment submitted by student " + userId);
        log.setMetadata(metadata);
        activityLogRepository.save(log);
    }

    @Transactional
    public void logLogin(UUID userId, UUID tenantId, String source) {
        Map<String, Object> metadata = new HashMap<>();
        if (source != null) {
            metadata.put("source", source);
        }
        ActivityLog log = new ActivityLog();
        log.setTenantId(tenantId);
        log.setUserId(userId);
        log.setActivityType(ActivityType.LOGIN);
        log.setDescription("User logged in");
        log.setMetadata(metadata);
        activityLogRepository.save(log);
    }

    @Transactional
    public void logLogout(UUID userId, UUID tenantId) {
        ActivityLog log = new ActivityLog();
        log.setTenantId(tenantId);
        log.setUserId(userId);
        log.setActivityType(ActivityType.LOGOUT);
        log.setDescription("User logged out");
        log.setMetadata(Map.of());
        activityLogRepository.save(log);
    }

    @Transactional
    public void logReportDownload(GeneratedReport report) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reportId", report.getId().toString());
        metadata.put("studentId", report.getStudent().getId().toString());
        metadata.put("fileName", report.getFileName());
        metadata.put("s3Key", report.getS3Key());

        ActivityLog log = new ActivityLog();
        log.setTenantId(report.getStudent().getTenant().getId());
        log.setUserId(SecurityUtils.requirePrincipal().getId());
        log.setActivityType(ActivityType.REPORT_DOWNLOAD);
        log.setDescription("Report downloaded: " + report.getFileName());
        log.setMetadata(metadata);
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> list(
            UUID userId,
            ActivityType activityType,
            Instant fromDate,
            Instant toDate,
            int page,
            int size
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Pageable pageable = PageRequest.of(page, size);

        Specification<ActivityLog> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (activityType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activityType"), activityType));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
        }

        Page<ActivityLog> logs = activityLogRepository.findAll(spec, pageable);
        return PageResponse.from(logs.map(this::toResponse));
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getUserId(),
                log.getActivityType(),
                log.getDescription(),
                log.getMetadata(),
                log.getCreatedAt()
        );
    }
}
