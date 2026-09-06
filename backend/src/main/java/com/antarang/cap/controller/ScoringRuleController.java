package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateScoringRuleRequest;
import com.antarang.cap.dto.request.CreateScoringRuleVersionRequest;
import com.antarang.cap.dto.request.UpdateScoringRuleVersionRequest;
import com.antarang.cap.dto.response.ScoringRuleResponse;
import com.antarang.cap.dto.response.ScoringRuleVersionResponse;
import com.antarang.cap.service.ScoringRuleService;
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
public class ScoringRuleController {

    private final ScoringRuleService scoringRuleService;

    public ScoringRuleController(ScoringRuleService scoringRuleService) {
        this.scoringRuleService = scoringRuleService;
    }

    @PostMapping("/scoring-rules")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ScoringRuleResponse> create(@Valid @RequestBody CreateScoringRuleRequest request) {
        return ApiResponse.success(scoringRuleService.create(request), "Scoring rule created successfully");
    }

    @GetMapping("/scoring-rules")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR')")
    public ApiResponse<List<ScoringRuleResponse>> list() {
        return ApiResponse.success(scoringRuleService.list());
    }

    @PostMapping("/scoring-rules/{scoringRuleId}/versions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ScoringRuleVersionResponse> createVersion(
            @PathVariable UUID scoringRuleId,
            @Valid @RequestBody CreateScoringRuleVersionRequest request
    ) {
        return ApiResponse.success(scoringRuleService.createVersion(scoringRuleId, request), "Scoring rule version created successfully");
    }

    @PutMapping("/scoring-rule-versions/{versionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ScoringRuleVersionResponse> updateVersion(
            @PathVariable UUID versionId,
            @Valid @RequestBody UpdateScoringRuleVersionRequest request
    ) {
        return ApiResponse.success(scoringRuleService.updateVersion(versionId, request), "Scoring rule version updated successfully");
    }

    @PostMapping("/scoring-rule-versions/{versionId}/publish")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ScoringRuleVersionResponse> publish(@PathVariable UUID versionId) {
        return ApiResponse.success(scoringRuleService.publish(versionId), "Scoring rule version published successfully");
    }

    @PostMapping("/scoring-rule-versions/{versionId}/retire")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ScoringRuleVersionResponse> retire(@PathVariable UUID versionId) {
        return ApiResponse.success(scoringRuleService.retire(versionId), "Scoring rule version retired successfully");
    }
}
