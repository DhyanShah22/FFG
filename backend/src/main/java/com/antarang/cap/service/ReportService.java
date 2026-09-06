package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.GeneratedReport;
import com.antarang.cap.domain.entity.Language;
import com.antarang.cap.domain.entity.RecommendationRun;
import com.antarang.cap.domain.entity.ReportTemplateVersion;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.ReportType;
import com.antarang.cap.domain.enums.RecommendationRunStatus;
import com.antarang.cap.dto.request.GenerateReportRequest;
import com.antarang.cap.dto.response.GenerateReportResponse;
import com.antarang.cap.dto.response.ReportDetailResponse;
import com.antarang.cap.dto.response.ReportDownloadResponse;
import com.antarang.cap.dto.response.ReportListItemResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import com.antarang.cap.repository.DomainScoreRepository;
import com.antarang.cap.repository.GeneratedReportRepository;
import com.antarang.cap.repository.LanguageRepository;
import com.antarang.cap.repository.RecommendationRunRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.StudentAccessGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {

    private final GeneratedReportRepository generatedReportRepository;
    private final RecommendationRunRepository recommendationRunRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final DomainScoreRepository domainScoreRepository;
    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;
    private final AuditService auditService;
    private final StudentAccessGuard studentAccessGuard;

    public ReportService(
            GeneratedReportRepository generatedReportRepository,
            RecommendationRunRepository recommendationRunRepository,
            AssessmentAttemptRepository attemptRepository,
            DomainScoreRepository domainScoreRepository,
            LanguageRepository languageRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            ActivityLogService activityLogService,
            AuditService auditService,
            StudentAccessGuard studentAccessGuard
    ) {
        this.generatedReportRepository = generatedReportRepository;
        this.recommendationRunRepository = recommendationRunRepository;
        this.attemptRepository = attemptRepository;
        this.domainScoreRepository = domainScoreRepository;
        this.languageRepository = languageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
        this.auditService = auditService;
        this.studentAccessGuard = studentAccessGuard;
    }

    @Transactional
    public GenerateReportResponse generate(UUID studentId, GenerateReportRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        studentAccessGuard.assertStudentAccess(studentId);

        User student = userRepository.findById(studentId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        UUID attemptId = request != null ? request.attemptId() : null;
        AssessmentAttempt attempt = resolveAttempt(studentId, attemptId, tenantId);

        if (domainScoreRepository.findByAttemptId(attempt.getId()).isEmpty()) {
            throw new BusinessException("Cannot generate report without domain scores", "NO_SCORES");
        }

        RecommendationRun recommendationRun = resolveRecommendationRun(studentId, request);

        ReportTemplateVersion templateVersion = attempt.getReportTemplateVersion();
        if (templateVersion == null) {
            throw new BusinessException("Report template version not found on attempt", "TEMPLATE_NOT_FOUND");
        }

        Language language = resolveLanguage(student, request, attempt);

        String fileName = buildFileName(student);
        String s3Key = "s3://antarang-cap-mock/reports/" + studentId + "/" + fileName;

        GeneratedReport report = new GeneratedReport();
        report.setStudent(student);
        report.setRecommendationRun(recommendationRun);
        report.setReportTemplateVersion(templateVersion);
        report.setConfigurationGroup(attempt.getConfigurationGroup());
        report.setReportType(ReportType.STUDENT);
        report.setLanguage(language);
        report.setS3Key(s3Key);
        report.setFileName(fileName);
        report.setGeneratedBy(SecurityUtils.requirePrincipal().getId());
        GeneratedReport saved = generatedReportRepository.save(report);

        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", student.getFirstName() != null ? student.getFirstName() : "Student");
        variables.put("reportUrl", "/api/v1/reports/" + saved.getId() + "/download");
        notificationService.enqueue(student.getId(), "REPORT_PUBLISHED", variables);

        return new GenerateReportResponse(
                saved.getId(),
                studentId,
                fileName,
                "/api/v1/reports/" + saved.getId() + "/download",
                language.getId(),
                saved.getGeneratedAt()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportListItemResponse> listReports(UUID studentId, UUID attemptId, int page, int size) {
        studentAccessGuard.assertStudentAccess(studentId);
        Pageable pageable = PageRequest.of(page, size);

        Page<GeneratedReport> reports;
        if (attemptId != null) {
            UUID tenantId = SecurityUtils.requireTenantId();
            AssessmentAttempt attempt = attemptRepository.findByIdAndTenantId(attemptId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
            if (!attempt.getStudent().getId().equals(studentId)) {
                throw new BusinessException("Attempt does not belong to student", "ACCESS_DENIED");
            }
            UUID configurationGroupId = attempt.getConfigurationGroup().getId();
            reports = generatedReportRepository.findByStudentIdAndConfigurationGroupId(studentId, configurationGroupId, pageable);
        } else {
            reports = generatedReportRepository.findByStudentId(studentId, pageable);
        }
        return PageResponse.from(reports.map(this::toListItem));
    }

    @Transactional(readOnly = true)
    public ReportDetailResponse getReport(UUID reportId) {
        GeneratedReport report = generatedReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        studentAccessGuard.assertStudentAccess(report.getStudent().getId());
        return toDetail(report);
    }

    @Transactional
    public ReportDownloadResponse download(UUID reportId) {
        GeneratedReport report = generatedReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        studentAccessGuard.assertStudentAccess(report.getStudent().getId());

        activityLogService.logReportDownload(report);

        Map<String, Object> auditValue = new HashMap<>();
        auditValue.put("reportId", report.getId().toString());
        auditValue.put("studentId", report.getStudent().getId().toString());
        auditValue.put("fileName", report.getFileName());
        auditService.record("generated_report", report.getId(), AuditAction.UPDATE, null, auditValue);

        return new ReportDownloadResponse(
                "https://example.invalid/mock/" + reportId + ".pdf",
                300
        );
    }

    private RecommendationRun resolveRecommendationRun(UUID studentId, GenerateReportRequest request) {
        if (request != null && request.recommendationRunId() != null) {
            return recommendationRunRepository.findByIdAndStudentId(request.recommendationRunId(), studentId)
                    .filter(r -> r.getStatus() == RecommendationRunStatus.COMPLETED)
                    .orElseThrow(() -> new BusinessException("Recommendation run not found or incomplete", "NO_RECOMMENDATION_RUN"));
        }
        return recommendationRunRepository
                .findTopByStudentIdAndStatusOrderByGeneratedAtDesc(studentId, RecommendationRunStatus.COMPLETED)
                .orElseThrow(() -> new BusinessException("No completed recommendation run found", "NO_RECOMMENDATION_RUN"));
    }

    private AssessmentAttempt resolveAttempt(UUID studentId, UUID attemptId, UUID tenantId) {
        if (attemptId != null) {
            AssessmentAttempt attempt = attemptRepository.findByIdAndTenantId(attemptId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
            if (!attempt.getStudent().getId().equals(studentId)) {
                throw new BusinessException("Attempt does not belong to student", "ACCESS_DENIED");
            }
            return attempt;
        }
        return attemptRepository.findTopByStudentIdAndStatusOrderBySubmittedAtDesc(studentId, AttemptStatus.SUBMITTED)
                .orElseThrow(() -> new BusinessException("No submitted attempt found", "INVALID_STATE"));
    }

    private Language resolveLanguage(User student, GenerateReportRequest request, AssessmentAttempt attempt) {
        if (request != null && request.languageId() != null) {
            return languageRepository.findById(request.languageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Language not found"));
        }
        if (student.getPreferredPlatformLanguageId() != null) {
            return languageRepository.findById(student.getPreferredPlatformLanguageId())
                    .orElse(attempt.getOutputLanguage());
        }
        return attempt.getOutputLanguage();
    }

    private String buildFileName(User student) {
        String first = student.getFirstName() != null ? student.getFirstName().toLowerCase().replaceAll("\\s+", "-") : "student";
        String last = student.getLastName() != null ? student.getLastName().toLowerCase().replaceAll("\\s+", "-") : "";
        String base = last.isEmpty() ? first : first + "-" + last;
        return "career-report-" + base + ".pdf";
    }

    private ReportListItemResponse toListItem(GeneratedReport report) {
        return new ReportListItemResponse(
                report.getId(),
                report.getStudent().getId(),
                report.getFileName(),
                "/api/v1/reports/" + report.getId() + "/download",
                report.getLanguage().getId(),
                report.getGeneratedAt()
        );
    }

    private ReportDetailResponse toDetail(GeneratedReport report) {
        return new ReportDetailResponse(
                report.getId(),
                report.getStudent().getId(),
                report.getReportTemplateVersion().getId(),
                report.getRecommendationRun().getId(),
                report.getConfigurationGroup().getId(),
                report.getReportType().name(),
                report.getLanguage().getId(),
                report.getFileName(),
                "/api/v1/reports/" + report.getId() + "/download",
                report.getS3Key(),
                report.getGeneratedAt(),
                report.getGeneratedBy()
        );
    }
}
