package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.dto.request.SubmitAttemptRequest;
import com.antarang.cap.dto.request.UpsertAttemptResponsesRequest;
import com.antarang.cap.dto.request.UpsertQuestionTimingRequest;
import com.antarang.cap.dto.response.AttemptDetailResponse;
import com.antarang.cap.dto.response.AttemptHistoryItemResponse;
import com.antarang.cap.dto.response.SubmitAttemptResponse;
import com.antarang.cap.service.AssessmentAttemptService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AttemptController {

    private final AssessmentAttemptService attemptService;

    public AttemptController(AssessmentAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<AttemptDetailResponse> getAttempt(@PathVariable UUID attemptId) {
        return ApiResponse.success(attemptService.getAttempt(attemptId));
    }

    @PutMapping("/attempts/{attemptId}/responses")
    @PreAuthorize("hasAnyRole('CAREER_EXPLORER','ADMINISTRATOR')")
    public ApiResponse<Void> saveResponses(
            @PathVariable UUID attemptId,
            @Valid @RequestBody UpsertAttemptResponsesRequest request
    ) {
        attemptService.saveResponses(attemptId, request);
        return ApiResponse.success(null, "Responses saved successfully");
    }

    @PutMapping("/attempts/{attemptId}/question-timings")
    @PreAuthorize("hasAnyRole('CAREER_EXPLORER','ADMINISTRATOR')")
    public ApiResponse<Void> saveTimings(
            @PathVariable UUID attemptId,
            @Valid @RequestBody UpsertQuestionTimingRequest request
    ) {
        attemptService.saveTimings(attemptId, request);
        return ApiResponse.success(null, "Question timings saved successfully");
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("hasAnyRole('CAREER_EXPLORER','ADMINISTRATOR')")
    public ApiResponse<SubmitAttemptResponse> submit(
            @PathVariable UUID attemptId,
            @RequestBody(required = false) SubmitAttemptRequest request
    ) {
        return ApiResponse.success(attemptService.submit(attemptId, request), "Attempt submitted successfully");
    }

    @GetMapping("/students/{studentId}/attempt-history")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<PageResponse<AttemptHistoryItemResponse>> attemptHistory(
            @PathVariable UUID studentId,
            @RequestParam(required = false) UUID assessmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(attemptService.attemptHistory(studentId, assessmentId, page, size));
    }
}
