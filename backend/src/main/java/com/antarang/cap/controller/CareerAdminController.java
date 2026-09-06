package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateCareerClusterRequest;
import com.antarang.cap.dto.request.CreateCareerMappingRequest;
import com.antarang.cap.dto.request.CreateCareerRequest;
import com.antarang.cap.dto.request.UpdateCareerClusterRequest;
import com.antarang.cap.dto.request.UpdateCareerMappingRequest;
import com.antarang.cap.dto.response.CareerClusterResponse;
import com.antarang.cap.dto.response.CareerMappingResponse;
import com.antarang.cap.dto.response.CareerResponse;
import com.antarang.cap.service.CareerService;
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
public class CareerAdminController {

    private final CareerService careerService;

    public CareerAdminController(CareerService careerService) {
        this.careerService = careerService;
    }

    @PostMapping("/career-clusters")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<CareerClusterResponse> createCluster(@Valid @RequestBody CreateCareerClusterRequest request) {
        return ApiResponse.success(careerService.createCluster(request), "Career cluster created successfully");
    }

    @GetMapping("/career-clusters")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<List<CareerClusterResponse>> listClusters() {
        return ApiResponse.success(careerService.listClusters());
    }

    @PutMapping("/career-clusters/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<CareerClusterResponse> updateCluster(
            @PathVariable UUID id,
            @RequestBody UpdateCareerClusterRequest request
    ) {
        return ApiResponse.success(careerService.updateCluster(id, request), "Career cluster updated successfully");
    }

    @PostMapping("/careers")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<CareerResponse> createCareer(@Valid @RequestBody CreateCareerRequest request) {
        return ApiResponse.success(careerService.createCareer(request), "Career created successfully");
    }

    @GetMapping("/careers")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<List<CareerResponse>> listCareers(@RequestParam(required = false) UUID clusterId) {
        return ApiResponse.success(careerService.listCareers(clusterId));
    }

    @PostMapping("/career-mappings")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<CareerMappingResponse> createMapping(@Valid @RequestBody CreateCareerMappingRequest request) {
        return ApiResponse.success(careerService.createMapping(request), "Career mapping created successfully");
    }

    @GetMapping("/career-mappings")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<List<CareerMappingResponse>> listMappings(@RequestParam(required = false) UUID careerId) {
        return ApiResponse.success(careerService.listMappings(careerId));
    }

    @PutMapping("/career-mappings/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<CareerMappingResponse> updateMapping(
            @PathVariable UUID id,
            @RequestBody UpdateCareerMappingRequest request
    ) {
        return ApiResponse.success(careerService.updateMapping(id, request), "Career mapping updated successfully");
    }
}
