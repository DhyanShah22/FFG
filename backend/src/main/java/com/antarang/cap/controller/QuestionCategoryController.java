package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.dto.request.CreateQuestionCategoryRequest;
import com.antarang.cap.dto.request.UpdateQuestionCategoryRequest;
import com.antarang.cap.dto.response.QuestionCategoryResponse;
import com.antarang.cap.service.QuestionCategoryService;
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
@RequestMapping("/api/v1/question-categories")
public class QuestionCategoryController {

    private final QuestionCategoryService questionCategoryService;

    public QuestionCategoryController(QuestionCategoryService questionCategoryService) {
        this.questionCategoryService = questionCategoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN', 'CAREER_COUNSELLOR')")
    public ApiResponse<PageResponse<QuestionCategoryResponse>> list(
            @RequestParam(required = false) AssessmentType assessmentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(questionCategoryService.list(assessmentType, page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionCategoryResponse> create(@Valid @RequestBody CreateQuestionCategoryRequest request) {
        return ApiResponse.success(questionCategoryService.create(request), "Question category created successfully");
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionCategoryResponse> update(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateQuestionCategoryRequest request
    ) {
        return ApiResponse.success(questionCategoryService.update(categoryId, request), "Question category updated successfully");
    }
}
