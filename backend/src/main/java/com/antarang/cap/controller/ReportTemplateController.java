package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateReportTemplateRequest;
import com.antarang.cap.dto.request.CreateReportTemplateVersionRequest;
import com.antarang.cap.dto.request.UpdateReportTemplateVersionRequest;
import com.antarang.cap.dto.response.ReportSectionResponse;
import com.antarang.cap.dto.response.ReportTemplateResponse;
import com.antarang.cap.dto.response.ReportTemplateVersionResponse;
import com.antarang.cap.service.ReportTemplateService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReportTemplateController {

    private final ReportTemplateService reportTemplateService;

    public ReportTemplateController(ReportTemplateService reportTemplateService) {
        this.reportTemplateService = reportTemplateService;
    }

    @PostMapping("/report-templates")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ReportTemplateResponse> create(@Valid @RequestBody CreateReportTemplateRequest request) {
        return ApiResponse.success(reportTemplateService.create(request), "Report template created successfully");
    }

    @GetMapping("/report-templates")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR')")
    public ApiResponse<List<ReportTemplateResponse>> list() {
        return ApiResponse.success(reportTemplateService.list());
    }

    @PostMapping("/report-templates/{id}/versions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ReportTemplateVersionResponse> createVersion(
            @PathVariable UUID id,
            @RequestBody CreateReportTemplateVersionRequest request
    ) {
        return ApiResponse.success(
                reportTemplateService.createVersion(id, request),
                "Report template version created successfully"
        );
    }

    @PutMapping("/report-template-versions/{versionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ReportTemplateVersionResponse> updateVersion(
            @PathVariable UUID versionId,
            @RequestBody UpdateReportTemplateVersionRequest request
    ) {
        return ApiResponse.success(
                reportTemplateService.updateVersion(versionId, request),
                "Report template version updated successfully"
        );
    }

    @GetMapping("/report-template-versions/{versionId}/sections")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR')")
    public ApiResponse<java.util.List<ReportSectionResponse>> listSections(@PathVariable UUID versionId) {
        return ApiResponse.success(reportTemplateService.listSections(versionId));
    }

    @PostMapping("/report-template-versions/{versionId}/publish")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ReportTemplateVersionResponse> publish(@PathVariable UUID versionId) {
        return ApiResponse.success(
                reportTemplateService.publish(versionId),
                "Report template version published successfully"
        );
    }
}
