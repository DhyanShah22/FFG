package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateBenchmarkRequest;
import com.antarang.cap.dto.request.UpdateBenchmarkRequest;
import com.antarang.cap.dto.response.BenchmarkResponse;
import com.antarang.cap.service.BenchmarkService;
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
@RequestMapping("/api/v1/benchmarks")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR')")
    public ApiResponse<List<BenchmarkResponse>> list(
            @RequestParam(required = false) UUID assessmentId,
            @RequestParam(required = false) String domainCode,
            @RequestParam(required = false) UUID gradeConfigId,
            @RequestParam(required = false) UUID ageGroupConfigId
    ) {
        return ApiResponse.success(benchmarkService.list(assessmentId, domainCode, gradeConfigId, ageGroupConfigId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<BenchmarkResponse> create(@Valid @RequestBody CreateBenchmarkRequest request) {
        return ApiResponse.success(benchmarkService.create(request), "Benchmark created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<BenchmarkResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateBenchmarkRequest request
    ) {
        return ApiResponse.success(benchmarkService.update(id, request), "Benchmark updated successfully");
    }
}
