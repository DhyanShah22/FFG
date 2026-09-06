package com.antarang.cap.security;

import com.antarang.cap.service.IntegrationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IntegrationApiKeyFilter extends OncePerRequestFilter {

    private final IntegrationService integrationService;

    public IntegrationApiKeyFilter(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/integrations/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String apiKey = extractApiKey(request);
        if (apiKey != null) {
            IntegrationClientPrincipal principal = integrationService.authenticateApiKey(apiKey);
            if (principal != null) {
                SecurityContextHolder.getContext().setAuthentication(principal);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractApiKey(HttpServletRequest request) {
        String header = request.getHeader("X-Api-Key");
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("ApiKey ")) {
            return auth.substring(7).trim();
        }
        return null;
    }
}
