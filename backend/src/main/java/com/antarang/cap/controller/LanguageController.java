package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateLanguageRequest;
import com.antarang.cap.dto.response.LanguageResponse;
import com.antarang.cap.service.LanguageService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/languages")
public class LanguageController {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping
    public ApiResponse<List<LanguageResponse>> listLanguages() {
        return ApiResponse.success(languageService.listLanguages());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<LanguageResponse> createLanguage(@Valid @RequestBody CreateLanguageRequest request) {
        return ApiResponse.success(languageService.createLanguage(request), "Language created successfully");
    }
}
