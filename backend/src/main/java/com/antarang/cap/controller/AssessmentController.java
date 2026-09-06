package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.dto.request.CreateAssessmentRequest;
import com.antarang.cap.dto.request.CreateAssessmentVersionRequest;
import com.antarang.cap.dto.request.UpsertAssessmentConfigurationRequest;
import com.antarang.cap.dto.response.AssessmentConfigurationResponse;
import com.antarang.cap.dto.response.AssessmentResponse;
import com.antarang.cap.dto.response.AssessmentVersionResponse;
import com.antarang.cap.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/assessments")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AssessmentResponse> create(@Valid @RequestBody CreateAssessmentRequest request) {
        return ApiResponse.success(assessmentService.create(request), "Assessment created successfully");
    }

    @GetMapping("/assessments")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR')")
    public ApiResponse<PageResponse<AssessmentResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String assessmentType
    ) {
        return ApiResponse.success(assessmentService.list(page, size, assessmentType));
    }

    @PostMapping("/assessments/{assessmentId}/versions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AssessmentVersionResponse> createVersion(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody CreateAssessmentVersionRequest request
    ) {
        return ApiResponse.success(
                assessmentService.createVersion(assessmentId, request),
                "Assessment version created successfully"
        );
    }

    @PostMapping("/assessment-versions/{versionId}/configuration")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AssessmentConfigurationResponse> upsertConfiguration(
            @PathVariable UUID versionId,
            @RequestBody UpsertAssessmentConfigurationRequest request
    ) {
        return ApiResponse.success(
                assessmentService.upsertConfiguration(versionId, request),
                "Assessment configuration saved successfully"
        );
    }
}
