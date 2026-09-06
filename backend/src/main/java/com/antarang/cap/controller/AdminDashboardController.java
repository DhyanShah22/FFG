package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.dto.response.AdminDashboardResponse;
import com.antarang.cap.service.DashboardService;
import com.antarang.cap.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN')")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final DashboardService dashboardService;

    public AdminDashboardController(UserRepository userRepository, DashboardService dashboardService) {
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> dashboard() {
        // Delegate to DashboardService for consistent admin metrics
        return ApiResponse.success(dashboardService.getAdminDashboard(null, null, null, null));
    }
}
