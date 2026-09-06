package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.response.StudentActiveAssessmentConfigurationResponse;
import com.antarang.cap.service.AssessmentConfigurationGroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
public class StudentAssessmentConfigurationController {

    private final AssessmentConfigurationGroupService groupService;

    public StudentAssessmentConfigurationController(AssessmentConfigurationGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/{studentId}/active-assessment-configuration")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SUB_ADMIN','CAREER_COUNSELLOR','CAREER_EXPLORER')")
    public ApiResponse<StudentActiveAssessmentConfigurationResponse> getActiveConfiguration(
            @PathVariable UUID studentId,
            @RequestParam(required = false) UUID assessmentId
    ) {
        return ApiResponse.success(groupService.getActiveForStudent(studentId, assessmentId));
    }
}
