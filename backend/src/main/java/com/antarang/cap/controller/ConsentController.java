package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateConsentRequest;
import com.antarang.cap.dto.response.ConsentResponse;
import com.antarang.cap.service.ConsentService;
import jakarta.validation.Valid;
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
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping("/consents")
    public ApiResponse<ConsentResponse> createConsent(@Valid @RequestBody CreateConsentRequest request) {
        return ApiResponse.success(consentService.createConsent(request), "Consent recorded successfully");
    }

    @GetMapping("/users/{userId}/consents")
    public ApiResponse<List<ConsentResponse>> listConsents(@PathVariable UUID userId) {
        return ApiResponse.success(consentService.listByUser(userId));
    }

    @PostMapping("/consents/{consentId}/withdraw")
    public ApiResponse<ConsentResponse> withdrawConsent(@PathVariable UUID consentId) {
        return ApiResponse.success(consentService.withdraw(consentId), "Consent withdrawn successfully");
    }
}
