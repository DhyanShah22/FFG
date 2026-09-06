package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.ReviewStatus;
import com.antarang.cap.dto.request.CreateQuestionOptionRequest;
import com.antarang.cap.dto.request.CreateQuestionRequest;
import com.antarang.cap.dto.request.CreateQuestionRuleRequest;
import com.antarang.cap.dto.request.UpdateQuestionRequest;
import com.antarang.cap.dto.request.UpdateQuestionStatusRequest;
import com.antarang.cap.dto.request.UpsertQuestionTranslationsRequest;
import com.antarang.cap.dto.response.QuestionDetailResponse;
import com.antarang.cap.dto.response.QuestionOptionResponse;
import com.antarang.cap.dto.response.QuestionRuleResponse;
import com.antarang.cap.dto.response.QuestionSummaryResponse;
import com.antarang.cap.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionDetailResponse> create(@Valid @RequestBody CreateQuestionRequest request) {
        return ApiResponse.success(questionService.create(request), "Question created successfully");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN', 'CAREER_COUNSELLOR')")
    public ApiResponse<PageResponse<QuestionSummaryResponse>> list(
            @RequestParam(required = false) AssessmentType assessmentType,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) ReviewStatus reviewStatus,
            @RequestParam(required = false) UUID languageId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(questionService.list(
                assessmentType, categoryId, questionType, reviewStatus, languageId, search, page, size
        ));
    }

    @GetMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN', 'CAREER_COUNSELLOR')")
    public ApiResponse<QuestionDetailResponse> getById(@PathVariable UUID questionId) {
        return ApiResponse.success(questionService.getById(questionId));
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionDetailResponse> update(
            @PathVariable UUID questionId,
            @Valid @RequestBody UpdateQuestionRequest request
    ) {
        return ApiResponse.success(questionService.update(questionId, request), "Question updated successfully");
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<Void> delete(@PathVariable UUID questionId) {
        questionService.softDelete(questionId);
        return ApiResponse.success(null, "Question deleted successfully");
    }

    @PostMapping("/{questionId}/options")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionOptionResponse> addOption(
            @PathVariable UUID questionId,
            @Valid @RequestBody CreateQuestionOptionRequest request
    ) {
        return ApiResponse.success(questionService.addOption(questionId, request), "Option added successfully");
    }

    @PutMapping("/{questionId}/translations")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionDetailResponse> upsertTranslations(
            @PathVariable UUID questionId,
            @Valid @RequestBody UpsertQuestionTranslationsRequest request
    ) {
        return ApiResponse.success(
                questionService.upsertTranslations(questionId, request),
                "Translations updated successfully"
        );
    }

    @PostMapping("/{questionId}/rules")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionRuleResponse> addRule(
            @PathVariable UUID questionId,
            @Valid @RequestBody CreateQuestionRuleRequest request
    ) {
        return ApiResponse.success(questionService.addRule(questionId, request), "Rule created successfully");
    }

    @GetMapping("/{questionId}/rules")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN', 'CAREER_COUNSELLOR')")
    public ApiResponse<List<QuestionRuleResponse>> listRules(@PathVariable UUID questionId) {
        return ApiResponse.success(questionService.listRules(questionId));
    }

    @DeleteMapping("/{questionId}/rules/{ruleId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<Void> deactivateRule(@PathVariable UUID questionId, @PathVariable UUID ruleId) {
        questionService.deactivateRule(questionId, ruleId);
        return ApiResponse.success(null, "Rule deactivated successfully");
    }

    @PatchMapping("/{questionId}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionDetailResponse> updateStatus(
            @PathVariable UUID questionId,
            @Valid @RequestBody UpdateQuestionStatusRequest request
    ) {
        return ApiResponse.success(questionService.updateStatus(questionId, request), "Question status updated successfully");
    }
}
