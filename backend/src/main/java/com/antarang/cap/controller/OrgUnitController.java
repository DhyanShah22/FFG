package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateOrgUnitRequest;
import com.antarang.cap.dto.request.UpdateOrgUnitRequest;
import com.antarang.cap.dto.response.OrgUnitResponse;
import com.antarang.cap.service.OrgUnitService;
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
@RequestMapping("/api/v1/org-units")
public class OrgUnitController {

    private final OrgUnitService orgUnitService;

    public OrgUnitController(OrgUnitService orgUnitService) {
        this.orgUnitService = orgUnitService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<OrgUnitResponse> create(@Valid @RequestBody CreateOrgUnitRequest request) {
        return ApiResponse.success(orgUnitService.create(request), "Org unit created successfully");
    }

    @GetMapping
    public ApiResponse<List<OrgUnitResponse>> list(@RequestParam(defaultValue = "tree") String view) {
        boolean asTree = !"flat".equalsIgnoreCase(view);
        return ApiResponse.success(orgUnitService.list(asTree));
    }

    @GetMapping("/tree")
    public ApiResponse<List<OrgUnitResponse>> tree() {
        return ApiResponse.success(orgUnitService.tree());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrgUnitResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(orgUnitService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<OrgUnitResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrgUnitRequest request
    ) {
        return ApiResponse.success(orgUnitService.update(id, request), "Org unit updated successfully");
    }
}
