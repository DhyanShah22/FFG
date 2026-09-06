package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.dto.request.GenerateRecommendationRequest;
import com.antarang.cap.dto.response.GenerateRecommendationResponse;
import com.antarang.cap.service.RecommendationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommendations/generate/{studentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<GenerateRecommendationResponse> generate(
            @PathVariable UUID studentId,
            @RequestBody(required = false) GenerateRecommendationRequest request
    ) {
        return ApiResponse.success(
                recommendationService.generate(studentId, request),
                "Recommendations generated successfully"
        );
    }

    @GetMapping("/students/{studentId}/recommendations")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<PageResponse<?>> list(
            @PathVariable UUID studentId,
            @RequestParam(required = false) UUID runId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(recommendationService.listRecommendations(studentId, runId, page, size));
    }
}
