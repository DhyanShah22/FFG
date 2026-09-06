package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CreateSignupSessionRequest;
import com.antarang.cap.dto.request.ExpireSignupOtpRequest;
import com.antarang.cap.dto.request.SendOtpRequest;
import com.antarang.cap.dto.request.UpdateSignupSessionRequest;
import com.antarang.cap.dto.request.VerifyOtpRequest;
import com.antarang.cap.dto.response.OtpChallengeResponse;
import com.antarang.cap.dto.response.RegisterResponse;
import com.antarang.cap.dto.response.SignupSessionResponse;
import com.antarang.cap.domain.enums.OtpChannel;
import com.antarang.cap.service.SignupSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/signup/sessions")
public class SignupSessionController {

    private final SignupSessionService signupSessionService;

    public SignupSessionController(SignupSessionService signupSessionService) {
        this.signupSessionService = signupSessionService;
    }

    @PostMapping
    public ApiResponse<SignupSessionResponse> createSession(@Valid @RequestBody CreateSignupSessionRequest request) {
        return ApiResponse.success(signupSessionService.createSession(request), "Signup session created");
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<SignupSessionResponse> getSession(@PathVariable UUID sessionId) {
        return ApiResponse.success(signupSessionService.getSession(sessionId));
    }

    @PutMapping("/{sessionId}")
    public ApiResponse<SignupSessionResponse> updateSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateSignupSessionRequest request
    ) {
        return ApiResponse.success(signupSessionService.updateSession(sessionId, request), "Signup session updated");
    }

    @PostMapping("/{sessionId}/otp/send")
    public ApiResponse<OtpChallengeResponse> sendOtp(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendOtpRequest request
    ) {
        return ApiResponse.success(signupSessionService.sendOtp(sessionId, request), "OTP sent");
    }

    @PostMapping("/{sessionId}/otp/resend")
    public ApiResponse<OtpChallengeResponse> resendOtp(
            @PathVariable UUID sessionId,
            @RequestBody SendOtpRequest request
    ) {
        OtpChannel channel = request != null && request.channel() != null ? request.channel() : OtpChannel.EMAIL;
        return ApiResponse.success(signupSessionService.resendOtp(sessionId, channel), "OTP resent");
    }

    @PostMapping("/{sessionId}/otp/verify")
    public ApiResponse<SignupSessionResponse> verifyOtp(
            @PathVariable UUID sessionId,
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        return ApiResponse.success(signupSessionService.verifyOtp(sessionId, request), "OTP verified");
    }

    @PostMapping("/{sessionId}/otp/expire")
    public ApiResponse<SignupSessionResponse> expireOtp(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ExpireSignupOtpRequest request
    ) {
        signupSessionService.expireSignupOtp(sessionId, request.channel());
        return ApiResponse.success(signupSessionService.getSession(sessionId), "OTP expired");
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> abandon(@PathVariable UUID sessionId) {
        signupSessionService.abandonSession(sessionId);
        return ApiResponse.success(null, "Signup session abandoned");
    }

    @PostMapping("/{sessionId}/complete")
    public ApiResponse<RegisterResponse> complete(
            @PathVariable UUID sessionId,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                signupSessionService.completeSession(sessionId, httpRequest),
                "User registered successfully"
        );
    }
}
