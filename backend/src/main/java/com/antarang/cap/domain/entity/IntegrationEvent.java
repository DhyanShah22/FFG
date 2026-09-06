package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.IntegrationEventDirection;
import com.antarang.cap.domain.enums.IntegrationEventStatus;
import com.antarang.cap.domain.enums.IntegrationEventType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "integration_events")
public class IntegrationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "integration_client_id", nullable = false)
    private IntegrationClient integrationClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private IntegrationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IntegrationEventDirection direction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", columnDefinition = "jsonb")
    private Map<String, Object> requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private Map<String, Object> responsePayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IntegrationEventStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public IntegrationClient getIntegrationClient() { return integrationClient; }
    public void setIntegrationClient(IntegrationClient integrationClient) { this.integrationClient = integrationClient; }
    public IntegrationEventType getEventType() { return eventType; }
    public void setEventType(IntegrationEventType eventType) { this.eventType = eventType; }
    public IntegrationEventDirection getDirection() { return direction; }
    public void setDirection(IntegrationEventDirection direction) { this.direction = direction; }
    public Map<String, Object> getRequestPayload() { return requestPayload; }
    public void setRequestPayload(Map<String, Object> requestPayload) { this.requestPayload = requestPayload; }
    public Map<String, Object> getResponsePayload() { return responsePayload; }
    public void setResponsePayload(Map<String, Object> responsePayload) { this.responsePayload = responsePayload; }
    public IntegrationEventStatus getStatus() { return status; }
    public void setStatus(IntegrationEventStatus status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
