package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.dto.request.AddAssessmentConfigurationGroupItemsRequest;
import com.antarang.cap.dto.request.AddAssessmentConfigurationGroupOutputRequest;
import com.antarang.cap.dto.request.CreateAssessmentConfigurationGroupAssignmentRequest;
import com.antarang.cap.dto.request.CreateAssessmentConfigurationGroupRequest;
import com.antarang.cap.dto.request.UpdateAssessmentConfigurationGroupRequest;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupAssignmentResponse;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupDetailResponse;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupItemResponse;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupOutputResponse;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupResponse;
import com.antarang.cap.dto.response.ConfigurationResolutionResponse;
import com.antarang.cap.service.AssessmentConfigurationGroupService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessment-configuration-groups")
public class AssessmentConfigurationGroupController {

    private final AssessmentConfigurationGroupService groupService;

    public AssessmentConfigurationGroupController(AssessmentConfigurationGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AssessmentConfigurationGroupResponse> create(
            @Valid @RequestBody CreateAssessmentConfigurationGroupRequest request
    ) {
        return ApiResponse.success(groupService.create(request), "Assessment configuration group created successfully");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR')")
    public ApiResponse<PageResponse<AssessmentConfigurationGroupResponse>> list(
            @RequestParam(required = false) LifecycleStatus status,
            @RequestParam(required = false) String academicYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(groupService.list(status, academicYear, page, size));
    }

    @GetMapping("/resolve")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN')")
    public ApiResponse<ConfigurationResolutionResponse> resolve(
            @RequestParam UUID studentId,
            @RequestParam(required = false) UUID assessmentId
    ) {
        return ApiResponse.success(groupService.resolve(studentId, assessmentId));
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR')")
    public ApiResponse<AssessmentConfigurationGroupDetailResponse> get(@PathVariable UUID groupId) {
        return ApiResponse.success(groupService.get(groupId));
    }

    @PutMapping("/{groupId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AssessmentConfigurationGroupResponse> update(
            @PathVariable UUID groupId,
            @RequestBody UpdateAssessmentConfigurationGroupRequest request
    ) {
        return ApiResponse.success(groupService.update(groupId, request), "Assessment configuration group updated successfully");
    }

    @PostMapping("/{groupId}/items")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<List<AssessmentConfigurationGroupItemResponse>> addItems(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddAssessmentConfigurationGroupItemsRequest request
    ) {
        return ApiResponse.success(groupService.addItems(groupId, request), "Configuration group items saved successfully");
    }

    @PostMapping("/{groupId}/outputs")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AssessmentConfigurationGroupOutputResponse> addOutput(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddAssessmentConfigurationGroupOutputRequest request
    ) {
        return ApiResponse.success(groupService.addOutput(groupId, request), "Configuration group output saved successfully");
    }

    @PostMapping("/{groupId}/assignments")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN')")
    public ApiResponse<AssessmentConfigurationGroupAssignmentResponse> assign(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateAssessmentConfigurationGroupAssignmentRequest request
    ) {
        return ApiResponse.success(groupService.assign(groupId, request), "Configuration group assigned successfully");
    }

    @DeleteMapping("/{groupId}/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN')")
    public ApiResponse<Void> deleteAssignment(
            @PathVariable UUID groupId,
            @PathVariable UUID assignmentId
    ) {
        groupService.deleteAssignment(groupId, assignmentId);
        return ApiResponse.success(null, "Assignment removed successfully");
    }

    @PostMapping("/{groupId}/activate")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AssessmentConfigurationGroupResponse> activate(@PathVariable UUID groupId) {
        return ApiResponse.success(groupService.activate(groupId), "Assessment configuration group activated successfully");
    }
}
