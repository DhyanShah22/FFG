package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateConfigurationGroupRequest;
import com.antarang.cap.dto.request.CreateConfigurationRequest;
import com.antarang.cap.dto.request.UpdateConfigurationRequest;
import com.antarang.cap.dto.response.ConfigurationGroupResponse;
import com.antarang.cap.dto.response.ConfigurationResponse;
import com.antarang.cap.service.ConfigurationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping("/configuration-groups")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ConfigurationGroupResponse> createGroup(
            @Valid @RequestBody CreateConfigurationGroupRequest request
    ) {
        return ApiResponse.success(configurationService.createGroup(request), "Configuration group created successfully");
    }

    @GetMapping("/configuration-groups")
    public ApiResponse<List<ConfigurationGroupResponse>> listGroups() {
        return ApiResponse.success(configurationService.listGroups());
    }

    @PostMapping("/configurations")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ConfigurationResponse> createConfiguration(
            @Valid @RequestBody CreateConfigurationRequest request
    ) {
        return ApiResponse.success(configurationService.createConfiguration(request), "Configuration created successfully");
    }

    @GetMapping("/configurations")
    public ApiResponse<List<ConfigurationResponse>> listConfigurations(
            @RequestParam(required = false) String groupCode,
            @RequestParam(required = false) UUID tenantId
    ) {
        return ApiResponse.success(configurationService.listConfigurations(groupCode, tenantId));
    }

    @PutMapping("/configurations/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ConfigurationResponse> updateConfiguration(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConfigurationRequest request
    ) {
        return ApiResponse.success(configurationService.updateConfiguration(id, request), "Configuration updated successfully");
    }
}
