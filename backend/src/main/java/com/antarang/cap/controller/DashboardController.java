package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.response.AdminDashboardResponse;
import com.antarang.cap.dto.response.FacilitatorDashboardResponse;
import com.antarang.cap.dto.response.StudentDashboardResponse;
import com.antarang.cap.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/students/{studentId}/dashboard", "/dashboards/student"})
    @PreAuthorize("hasRole('CAREER_EXPLORER')")
    public ApiResponse<StudentDashboardResponse> studentDashboard(
            @PathVariable(required = false) UUID studentId
    ) {
        if (studentId == null) {
            studentId = com.antarang.cap.security.SecurityUtils.requirePrincipal().getId();
        }
        return ApiResponse.success(dashboardService.getStudentDashboard(studentId));
    }

    @GetMapping({"/facilitators/{facilitatorId}/dashboard", "/dashboards/counsellor"})
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','CAREER_COUNSELLOR')")
    public ApiResponse<FacilitatorDashboardResponse> facilitatorDashboard(
            @PathVariable(required = false) UUID facilitatorId
    ) {
        if (facilitatorId == null) {
            facilitatorId = com.antarang.cap.security.SecurityUtils.requirePrincipal().getId();
        }
        return ApiResponse.success(dashboardService.getFacilitatorDashboard(facilitatorId));
    }

    @GetMapping({"/admin/dashboard", "/dashboards/admin"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AdminDashboardResponse> adminDashboard(
            @RequestParam(required = false) UUID orgUnitId,
            @RequestParam(required = false) UUID clusterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate
    ) {
        return ApiResponse.success(dashboardService.getAdminDashboard(orgUnitId, clusterId, fromDate, toDate));
    }

    @GetMapping("/sub-admin/dashboard")
    @PreAuthorize("hasRole('SUB_ADMIN')")
    public ApiResponse<AdminDashboardResponse> subAdminDashboard(
            @RequestParam(required = false) UUID orgUnitId,
            @RequestParam(required = false) UUID clusterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate
    ) {
        return ApiResponse.success(dashboardService.getSubAdminDashboard(orgUnitId, clusterId, fromDate, toDate));
    }
}
