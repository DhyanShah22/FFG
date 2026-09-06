package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.dto.request.CreateFeedbackRequest;
import com.antarang.cap.dto.request.UpdateFeedbackRequest;
import com.antarang.cap.dto.response.FeedbackResponse;
import com.antarang.cap.service.FeedbackService;
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
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/students/{studentId}/feedback")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','CAREER_COUNSELLOR')")
    public ApiResponse<FeedbackResponse> create(
            @PathVariable UUID studentId,
            @Valid @RequestBody CreateFeedbackRequest request
    ) {
        return ApiResponse.success(feedbackService.create(studentId, request), "Feedback created successfully");
    }

    @GetMapping("/students/{studentId}/feedback")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<PageResponse<FeedbackResponse>> list(
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(feedbackService.list(studentId, page, size));
    }

    @PutMapping("/feedback/{feedbackId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','CAREER_COUNSELLOR')")
    public ApiResponse<FeedbackResponse> update(
            @PathVariable UUID feedbackId,
            @RequestBody UpdateFeedbackRequest request
    ) {
        return ApiResponse.success(feedbackService.update(feedbackId, request), "Feedback updated successfully");
    }
}
