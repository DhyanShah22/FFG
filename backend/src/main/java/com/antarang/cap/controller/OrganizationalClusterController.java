package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.AddClusterMemberRequest;
import com.antarang.cap.dto.request.CreateOrganizationalClusterRequest;
import com.antarang.cap.dto.response.ClusterMemberResponse;
import com.antarang.cap.dto.response.OrganizationalClusterResponse;
import com.antarang.cap.service.OrganizationalClusterService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizational-clusters")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class OrganizationalClusterController {

    private final OrganizationalClusterService organizationalClusterService;

    public OrganizationalClusterController(OrganizationalClusterService organizationalClusterService) {
        this.organizationalClusterService = organizationalClusterService;
    }

    @PostMapping
    public ApiResponse<OrganizationalClusterResponse> create(
            @Valid @RequestBody CreateOrganizationalClusterRequest request
    ) {
        return ApiResponse.success(organizationalClusterService.create(request), "Cluster created successfully");
    }

    @PostMapping("/{clusterId}/members")
    public ApiResponse<ClusterMemberResponse> addMember(
            @PathVariable UUID clusterId,
            @Valid @RequestBody AddClusterMemberRequest request
    ) {
        return ApiResponse.success(organizationalClusterService.addMember(clusterId, request), "Member added successfully");
    }

    @GetMapping("/{clusterId}/members")
    public ApiResponse<List<ClusterMemberResponse>> listMembers(@PathVariable UUID clusterId) {
        return ApiResponse.success(organizationalClusterService.listMembers(clusterId));
    }
}
