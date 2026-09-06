package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CalculateScoringRequest;
import com.antarang.cap.dto.response.CalculateScoringResponse;
import com.antarang.cap.dto.response.StudentScoresResponse;
import com.antarang.cap.service.ScoringService;
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
public class ScoringController {

    private final ScoringService scoringService;

    public ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @PostMapping("/scoring/calculate/{attemptId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<CalculateScoringResponse> calculate(
            @PathVariable UUID attemptId,
            @RequestBody(required = false) CalculateScoringRequest request
    ) {
        boolean force = request != null && Boolean.TRUE.equals(request.force());
        return ApiResponse.success(scoringService.calculate(attemptId, force), "Scoring completed successfully");
    }

    @GetMapping("/students/{studentId}/scores")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<StudentScoresResponse> getScores(
            @PathVariable UUID studentId,
            @RequestParam(required = false) UUID attemptId
    ) {
        return ApiResponse.success(scoringService.getStudentScores(studentId, attemptId));
    }
}
