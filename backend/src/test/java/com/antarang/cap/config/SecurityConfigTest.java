package com.antarang.cap.config;

import com.antarang.cap.controller.AuthController;
import com.antarang.cap.controller.SignupSessionController;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.dto.response.SignupSessionResponse;
import com.antarang.cap.dto.response.LoginResponse;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.CustomUserDetailsService;
import com.antarang.cap.security.IntegrationApiKeyFilter;
import com.antarang.cap.security.JwtAuthenticationFilter;
import com.antarang.cap.security.JwtService;
import com.antarang.cap.security.TokenDenylistService;
import com.antarang.cap.service.AuthService;
import com.antarang.cap.service.GoogleAuthService;
import com.antarang.cap.service.IntegrationService;
import com.antarang.cap.service.SignupSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that {@link SecurityConfig} permits anonymous access to the sign-up
 * and login entry points while still requiring authentication for protected
 * endpoints. These tests guard against regressions such as auth endpoints
 * being unintentionally blocked (403) by Spring Security.
 */
@WebMvcTest(controllers = {AuthController.class, SignupSessionController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        IntegrationApiKeyFilter.class,
        CustomUserDetailsService.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private SignupSessionService signupSessionService;

    @MockitoBean
    private GoogleAuthService googleAuthService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenDenylistService tokenDenylistService;

    @MockitoBean
    private IntegrationService integrationService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void signupEndpointIsAccessibleToAnonymousUsers() throws Exception {
        when(signupSessionService.createSession(any())).thenReturn(new SignupSessionResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ProfileType.CAREER_EXPLORER,
                false,
                false,
                Instant.now(),
                Instant.now(),
                Map.of()
        ));

        var result = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenantId":"11111111-1111-1111-1111-111111111111","profileType":"CAREER_EXPLORER"}
                                """))
                .andReturn();

        assertNotEquals(403, result.getResponse().getStatus(),
                "Anonymous signup requests must not be blocked by Spring Security");
    }

    @Test
    void signupSessionsEndpointIsAccessibleToAnonymousUsers() throws Exception {
        when(signupSessionService.createSession(any())).thenReturn(new SignupSessionResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ProfileType.CAREER_EXPLORER,
                false,
                false,
                Instant.now(),
                Instant.now(),
                Map.of()
        ));

        var result = mockMvc.perform(post("/api/v1/auth/signup/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenantId":"11111111-1111-1111-1111-111111111111","profileType":"CAREER_EXPLORER"}
                                """))
                .andReturn();

        assertNotEquals(403, result.getResponse().getStatus(),
                "Anonymous signup session creation must not be blocked by Spring Security");
    }

    @Test
    void loginEndpointIsAccessibleToAnonymousUsers() throws Exception {
        when(authService.login(any(), any())).thenReturn(new LoginResponse(
                "access-token", "refresh-token", 3600L, null, "CAREER_EXPLORER", null
        ));

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"someone@test.com","password":"whatever"}
                                """))
                .andReturn();

        assertNotEquals(403, result.getResponse().getStatus(),
                "Anonymous login requests must not be blocked by Spring Security");
    }

    @Test
    void protectedEndpointStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isForbidden());
    }
}
