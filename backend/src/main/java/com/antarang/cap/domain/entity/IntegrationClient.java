package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.IntegrationClientType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "integration_clients",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "client_code"})
)
public class IntegrationClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "client_code", nullable = false, length = 100)
    private String clientCode;

    @Column(name = "client_name", nullable = false, length = 200)
    private String clientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 50)
    private IntegrationClientType clientType;

    @Column(name = "api_key_hash", columnDefinition = "TEXT")
    private String apiKeyHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_scopes", columnDefinition = "jsonb")
    private Map<String, Object> allowedScopes;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public String getClientCode() { return clientCode; }
    public void setClientCode(String clientCode) { this.clientCode = clientCode; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public IntegrationClientType getClientType() { return clientType; }
    public void setClientType(IntegrationClientType clientType) { this.clientType = clientType; }
    public String getApiKeyHash() { return apiKeyHash; }
    public void setApiKeyHash(String apiKeyHash) { this.apiKeyHash = apiKeyHash; }
    public Map<String, Object> getAllowedScopes() { return allowedScopes; }
    public void setAllowedScopes(Map<String, Object> allowedScopes) { this.allowedScopes = allowedScopes; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
