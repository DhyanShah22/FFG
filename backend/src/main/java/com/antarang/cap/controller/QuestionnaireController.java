package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.dto.request.CloneQuestionnaireRequest;
import com.antarang.cap.dto.request.CreateQuestionnaireRequest;
import com.antarang.cap.dto.request.CreateQuestionnaireVersionRequest;
import com.antarang.cap.dto.request.PublishQuestionnaireVersionRequest;
import com.antarang.cap.dto.request.ReorderQuestionnaireQuestionsRequest;
import com.antarang.cap.dto.request.SetQuestionnaireQuestionsRequest;
import com.antarang.cap.dto.request.UpdateQuestionnaireRequest;
import com.antarang.cap.dto.response.QuestionnaireListItemResponse;
import com.antarang.cap.dto.response.QuestionnairePreviewResponse;
import com.antarang.cap.dto.response.QuestionnaireResponse;
import com.antarang.cap.dto.response.QuestionnaireVersionSummaryResponse;
import com.antarang.cap.service.QuestionnaireService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    @PostMapping("/questionnaires")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> create(@Valid @RequestBody CreateQuestionnaireRequest request) {
        return ApiResponse.success(questionnaireService.create(request), "Questionnaire created successfully");
    }

    @GetMapping("/questionnaires")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN', 'CAREER_COUNSELLOR')")
    public ApiResponse<PageResponse<QuestionnaireListItemResponse>> list(
            @RequestParam(required = false) AssessmentType assessmentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(questionnaireService.list(assessmentType, page, size));
    }

    @GetMapping("/questionnaires/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN', 'CAREER_COUNSELLOR')")
    public ApiResponse<QuestionnaireResponse> getById(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID versionId
    ) {
        return ApiResponse.success(questionnaireService.getById(id, versionId));
    }

    @PutMapping("/questionnaires/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuestionnaireRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.update(id, request),
                "Questionnaire updated successfully"
        );
    }

    @PostMapping("/questionnaires/{id}/versions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireVersionSummaryResponse> createVersion(
            @PathVariable UUID id,
            @RequestBody(required = false) CreateQuestionnaireVersionRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.createVersion(id, request != null ? request : new CreateQuestionnaireVersionRequest(null)),
                "Questionnaire version created successfully"
        );
    }

    @GetMapping("/questionnaires/{id}/versions")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN', 'CAREER_COUNSELLOR')")
    public ApiResponse<List<QuestionnaireVersionSummaryResponse>> listVersions(@PathVariable UUID id) {
        return ApiResponse.success(questionnaireService.listVersions(id));
    }

    @PostMapping("/questionnaires/{id}/clone")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> clone(
            @PathVariable UUID id,
            @RequestBody(required = false) CloneQuestionnaireRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.clone(id, request != null ? request : new CloneQuestionnaireRequest(null, null)),
                "Questionnaire cloned successfully"
        );
    }

    @PostMapping("/questionnaires/{id}/publish")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> publishCurrentDraft(
            @PathVariable UUID id,
            @RequestBody(required = false) PublishQuestionnaireVersionRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.publishCurrentDraft(id, request),
                "Questionnaire version published successfully"
        );
    }

    @PostMapping("/questionnaire-versions/{versionId}/questions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> setQuestions(
            @PathVariable UUID versionId,
            @Valid @RequestBody SetQuestionnaireQuestionsRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.setQuestions(versionId, request),
                "Questionnaire questions updated successfully"
        );
    }

    @PutMapping("/questionnaire-versions/{versionId}/questions/reorder")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> reorderQuestions(
            @PathVariable UUID versionId,
            @Valid @RequestBody ReorderQuestionnaireQuestionsRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.reorderQuestions(versionId, request),
                "Questionnaire questions reordered successfully"
        );
    }

    @GetMapping("/questionnaire-versions/{versionId}/preview")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUB_ADMIN', 'CAREER_COUNSELLOR')")
    public ApiResponse<QuestionnairePreviewResponse> preview(
            @PathVariable UUID versionId,
            @RequestParam(required = false) UUID languageId
    ) {
        return ApiResponse.success(questionnaireService.preview(versionId, languageId));
    }

    @PostMapping("/questionnaire-versions/{versionId}/publish")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> publishVersion(
            @PathVariable UUID versionId,
            @RequestBody(required = false) PublishQuestionnaireVersionRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.publishVersion(versionId, request),
                "Questionnaire version published successfully"
        );
    }

    @PostMapping("/questionnaire-versions/{versionId}/suspend")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> suspendVersion(@PathVariable UUID versionId) {
        return ApiResponse.success(
                questionnaireService.suspendVersion(versionId),
                "Questionnaire suspended successfully"
        );
    }

    @PostMapping("/questionnaire-versions/{versionId}/retire")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<QuestionnaireResponse> retireVersion(@PathVariable UUID versionId) {
        return ApiResponse.success(
                questionnaireService.retireVersion(versionId),
                "Questionnaire version retired successfully"
        );
    }
}
