package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.StartAssessmentRequest;
import com.antarang.cap.dto.response.StartAssessmentResponse;
import com.antarang.cap.dto.response.StudentAssessmentListItemResponse;
import com.antarang.cap.service.AssessmentAttemptService;
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
@RequestMapping("/api/v1")
public class StudentAssessmentController {

    private final AssessmentAttemptService attemptService;

    public StudentAssessmentController(AssessmentAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @GetMapping("/students/{studentId}/assessments")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<List<StudentAssessmentListItemResponse>> listAssessments(@PathVariable UUID studentId) {
        return ApiResponse.success(attemptService.listStudentAssessments(studentId));
    }

    @PostMapping("/assessments/{assessmentId}/start")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','CAREER_COUNSELLOR','CAREER_EXPLORER','SUB_ADMIN')")
    public ApiResponse<StartAssessmentResponse> start(
            @PathVariable UUID assessmentId,
            @RequestBody(required = false) StartAssessmentRequest request
    ) {
        return ApiResponse.success(attemptService.start(assessmentId, request), "Assessment started successfully");
    }
}
