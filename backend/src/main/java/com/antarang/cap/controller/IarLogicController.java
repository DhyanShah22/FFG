package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateIarLogicConfigRequest;
import com.antarang.cap.dto.request.CreateIarLogicVersionRequest;
import com.antarang.cap.dto.request.UpdateIarLogicVersionRequest;
import com.antarang.cap.dto.response.IarLogicConfigResponse;
import com.antarang.cap.dto.response.IarLogicVersionResponse;
import com.antarang.cap.dto.response.IarValidationResponse;
import com.antarang.cap.service.IarLogicService;
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
public class IarLogicController {

    private final IarLogicService iarLogicService;

    public IarLogicController(IarLogicService iarLogicService) {
        this.iarLogicService = iarLogicService;
    }

    @PostMapping("/iar-logic-configs")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<IarLogicConfigResponse> createConfig(@Valid @RequestBody CreateIarLogicConfigRequest request) {
        return ApiResponse.success(iarLogicService.createConfig(request), "IAR logic config created successfully");
    }

    @GetMapping("/iar-logic-configs")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR')")
    public ApiResponse<List<IarLogicConfigResponse>> listConfigs() {
        return ApiResponse.success(iarLogicService.listConfigs());
    }

    @PostMapping("/iar-logic-configs/{id}/versions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<IarLogicVersionResponse> createVersion(
            @PathVariable UUID id,
            @Valid @RequestBody CreateIarLogicVersionRequest request
    ) {
        return ApiResponse.success(iarLogicService.createVersion(id, request), "IAR logic version created successfully");
    }

    @PutMapping("/iar-logic-versions/{versionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<IarLogicVersionResponse> updateVersion(
            @PathVariable UUID versionId,
            @Valid @RequestBody UpdateIarLogicVersionRequest request
    ) {
        return ApiResponse.success(iarLogicService.updateVersion(versionId, request), "IAR logic version updated successfully");
    }

    @PostMapping("/iar-logic-versions/{versionId}/validate")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<IarValidationResponse> validate(@PathVariable UUID versionId) {
        return ApiResponse.success(iarLogicService.validate(versionId));
    }

    @PostMapping("/iar-logic-versions/{versionId}/publish")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<IarLogicVersionResponse> publish(@PathVariable UUID versionId) {
        return ApiResponse.success(iarLogicService.publish(versionId), "IAR logic version published successfully");
    }

    @PostMapping("/iar-logic-versions/{versionId}/retire")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<IarLogicVersionResponse> retire(@PathVariable UUID versionId) {
        return ApiResponse.success(iarLogicService.retire(versionId), "IAR logic version retired successfully");
    }
}
