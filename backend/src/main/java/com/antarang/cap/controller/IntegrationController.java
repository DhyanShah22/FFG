package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.enums.IntegrationEventStatus;
import com.antarang.cap.domain.enums.IntegrationEventType;
import com.antarang.cap.dto.request.CreateIntegrationClientRequest;
import com.antarang.cap.dto.request.IntegrationCreateStudentRequest;
import com.antarang.cap.dto.response.CreateIntegrationClientResponse;
import com.antarang.cap.dto.response.IntegrationEventResponse;
import com.antarang.cap.dto.response.IntegrationLatestReportResponse;
import com.antarang.cap.dto.response.IntegrationStudentResultsResponse;
import com.antarang.cap.service.IntegrationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping("/integration-clients")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<CreateIntegrationClientResponse> createClient(
            @Valid @RequestBody CreateIntegrationClientRequest request
    ) {
        return ApiResponse.success(integrationService.createClient(request), "Integration client created successfully");
    }

    @PostMapping("/integrations/students")
    @PreAuthorize("hasRole('INTEGRATION_CLIENT')")
    public ApiResponse<Map<String, Object>> createStudent(
            @Valid @RequestBody IntegrationCreateStudentRequest request
    ) {
        return ApiResponse.success(integrationService.createStudent(request), "Student upserted successfully");
    }

    @GetMapping("/integrations/students/{externalStudentId}/results")
    @PreAuthorize("hasRole('INTEGRATION_CLIENT')")
    public ApiResponse<IntegrationStudentResultsResponse> getResults(
            @PathVariable String externalStudentId
    ) {
        return ApiResponse.success(integrationService.getStudentResults(externalStudentId));
    }

    @GetMapping("/integrations/students/{externalStudentId}/reports/latest")
    @PreAuthorize("hasRole('INTEGRATION_CLIENT')")
    public ApiResponse<IntegrationLatestReportResponse> getLatestReport(
            @PathVariable String externalStudentId
    ) {
        return ApiResponse.success(integrationService.getLatestReport(externalStudentId));
    }

    @GetMapping("/integration-events")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN')")
    public ApiResponse<PageResponse<IntegrationEventResponse>> listEvents(
            @RequestParam(required = false) UUID integrationClientId,
            @RequestParam(required = false) IntegrationEventType eventType,
            @RequestParam(required = false) IntegrationEventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(integrationService.listEvents(
                integrationClientId, eventType, status, fromDate, toDate, page, size));
    }
}
