package com.antarang.cap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public class GoogleAuthProperties {

    /** Google OAuth client ID used to verify ID tokens. */
    private String clientId = "";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank();
    }
}
