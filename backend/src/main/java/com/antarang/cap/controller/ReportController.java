package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.dto.request.GenerateReportRequest;
import com.antarang.cap.dto.response.GenerateReportResponse;
import com.antarang.cap.dto.response.ReportDetailResponse;
import com.antarang.cap.dto.response.ReportDownloadResponse;
import com.antarang.cap.dto.response.ReportListItemResponse;
import com.antarang.cap.service.ReportService;
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
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/reports/generate/{studentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<GenerateReportResponse> generate(
            @PathVariable UUID studentId,
            @RequestBody(required = false) GenerateReportRequest request
    ) {
        return ApiResponse.success(reportService.generate(studentId, request), "Report generated successfully");
    }

    @GetMapping("/students/{studentId}/reports")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<PageResponse<ReportListItemResponse>> list(
            @PathVariable UUID studentId,
            @RequestParam(required = false) UUID attemptId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(reportService.listReports(studentId, attemptId, page, size));
    }

    @GetMapping("/reports/{reportId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<ReportDetailResponse> get(@PathVariable UUID reportId) {
        return ApiResponse.success(reportService.getReport(reportId));
    }

    @GetMapping("/reports/{reportId}/download")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<ReportDownloadResponse> download(@PathVariable UUID reportId) {
        return ApiResponse.success(reportService.download(reportId), "Report download URL generated");
    }
}
