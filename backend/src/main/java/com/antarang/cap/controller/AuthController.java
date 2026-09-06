package com.antarang.cap.controller;

import com.antarang.cap.common.ApiResponse;
import com.antarang.cap.dto.request.CompleteSignupRequest;
import com.antarang.cap.dto.request.ForgotPasswordRequest;
import com.antarang.cap.dto.request.GoogleLoginRequest;
import com.antarang.cap.dto.request.LoginRequest;
import com.antarang.cap.dto.request.LogoutRequest;
import com.antarang.cap.dto.request.RefreshTokenRequest;
import com.antarang.cap.dto.request.ResendPasswordResetOtpRequest;
import com.antarang.cap.dto.request.ResetPasswordRequest;
import com.antarang.cap.dto.response.AuthMeResponse;
import com.antarang.cap.dto.response.LoginResponse;
import com.antarang.cap.dto.response.OtpChallengeResponse;
import com.antarang.cap.dto.response.RegisterResponse;
import com.antarang.cap.service.AuthService;
import com.antarang.cap.service.GoogleAuthService;
import com.antarang.cap.service.SignupSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SignupSessionService signupSessionService;
    private final GoogleAuthService googleAuthService;

    public AuthController(
            AuthService authService,
            SignupSessionService signupSessionService,
            GoogleAuthService googleAuthService
    ) {
        this.authService = authService;
        this.signupSessionService = signupSessionService;
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(authService.login(request, httpRequest), "Login successful");
    }

    @PostMapping("/google")
    public ApiResponse<LoginResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(googleAuthService.login(request, httpRequest), "Login successful");
    }

    /**
     * Completes sign-up after OTP verification via signup session flow.
     */
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(
            @Valid @RequestBody CompleteSignupRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                signupSessionService.completeSession(request.signupSessionId(), httpRequest),
                "User registered successfully"
        );
    }

    @PostMapping({"/refresh", "/refresh-token"})
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request), "Token refreshed successfully");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest
    ) {
        String header = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
        String refreshToken = request != null ? request.refreshToken() : null;
        authService.logout(accessToken, refreshToken);
        return ApiResponse.success(null, "Logged out successfully");
    }

    @GetMapping("/me")
    public ApiResponse<AuthMeResponse> me() {
        return ApiResponse.success(authService.me(), "User profile fetched successfully");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<OtpChallengeResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        OtpChallengeResponse challenge = authService.forgotPassword(request, httpRequest);
        if (challenge == null) {
            return ApiResponse.success(null, "If an account exists for that identifier, an OTP has been sent");
        }
        return ApiResponse.success(challenge, "OTP sent");
    }

    @PostMapping("/forgot-password/resend")
    public ApiResponse<OtpChallengeResponse> resendForgotPasswordOtp(
            @Valid @RequestBody ResendPasswordResetOtpRequest request
    ) {
        return ApiResponse.success(authService.resendPasswordResetOtp(request), "OTP resent");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success(null, "Password has been reset successfully");
    }
}
