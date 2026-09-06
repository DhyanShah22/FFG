package com.antarang.cap.service;

import com.antarang.cap.config.GoogleAuthProperties;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.dto.request.GoogleLoginRequest;
import com.antarang.cap.dto.response.LoginResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.UserPrincipal;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class GoogleAuthService {

    private final GoogleAuthProperties googleAuthProperties;
    private final UserRepository userRepository;
    private final AuthService authService;

    public GoogleAuthService(
            GoogleAuthProperties googleAuthProperties,
            UserRepository userRepository,
            AuthService authService
    ) {
        this.googleAuthProperties = googleAuthProperties;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(GoogleLoginRequest request, HttpServletRequest httpRequest) {
        if (!googleAuthProperties.isConfigured()) {
            throw new BusinessException("Google SSO is not configured", "SSO_NOT_CONFIGURED");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        ).setAudience(Collections.singletonList(googleAuthProperties.getClientId())).build();

        try {
            GoogleIdToken idToken = verifier.verify(request.idToken());
            if (idToken == null) {
                throw new BusinessException("Invalid Google ID token", "SSO_INVALID_TOKEN");
            }

            String email = idToken.getPayload().getEmail();
            if (email == null || email.isBlank()) {
                throw new BusinessException("Google account email is required", "SSO_INVALID_TOKEN");
            }

            User user = userRepository.findByLoginIdWithRoles(email.trim().toLowerCase())
                    .orElseThrow(() -> new BusinessException("Google account is not registered", "SSO_USER_NOT_FOUND"));

            if (request.profileType() != user.getProfileType()) {
                throw new BusinessException("The selected account profile does not match this user", "PROFILE_MISMATCH");
            }

            return authService.loginAuthenticatedUser(user, UserPrincipal.from(user), httpRequest);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Failed to verify Google ID token", "SSO_INVALID_TOKEN");
        }
    }
}
