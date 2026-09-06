package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.AssignStudentsRequest;
import com.antarang.cap.dto.response.AssignmentHistoryResponse;
import com.antarang.cap.dto.response.FacilitatorStudentResponse;
import com.antarang.cap.service.FacilitatorAssignmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class FacilitatorAssignmentController {

    private final FacilitatorAssignmentService facilitatorAssignmentService;

    public FacilitatorAssignmentController(FacilitatorAssignmentService facilitatorAssignmentService) {
        this.facilitatorAssignmentService = facilitatorAssignmentService;
    }

    @PostMapping("/facilitators/{facilitatorId}/students")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN')")
    public ApiResponse<List<FacilitatorStudentResponse>> assignStudents(
            @PathVariable UUID facilitatorId,
            @Valid @RequestBody AssignStudentsRequest request
    ) {
        return ApiResponse.success(
                facilitatorAssignmentService.assignStudents(facilitatorId, request),
                "Students assigned successfully"
        );
    }

    @GetMapping("/facilitators/{facilitatorId}/students")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN') or authentication.principal.id == #facilitatorId")
    public ApiResponse<List<FacilitatorStudentResponse>> listStudents(@PathVariable UUID facilitatorId) {
        return ApiResponse.success(facilitatorAssignmentService.listStudents(facilitatorId));
    }

    @DeleteMapping("/facilitators/{facilitatorId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN')")
    public ApiResponse<Void> unassignStudent(
            @PathVariable UUID facilitatorId,
            @PathVariable UUID studentId
    ) {
        facilitatorAssignmentService.unassignStudent(facilitatorId, studentId);
        return ApiResponse.success(null, "Student unassigned successfully");
    }

    @GetMapping("/students/{studentId}/assignment-history")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN') or authentication.principal.id == #studentId")
    public ApiResponse<List<AssignmentHistoryResponse>> assignmentHistory(@PathVariable UUID studentId) {
        return ApiResponse.success(facilitatorAssignmentService.assignmentHistory(studentId));
    }
}
