package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.NotificationDeliveryChannel;
import com.antarang.cap.domain.enums.NotificationLogStatus;
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
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private NotificationTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationDeliveryChannel channel;

    @Column(nullable = false, length = 255)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationLogStatus status = NotificationLogStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_response", columnDefinition = "jsonb")
    private Map<String, Object> providerResponse;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public NotificationTemplate getTemplate() { return template; }
    public void setTemplate(NotificationTemplate template) { this.template = template; }
    public NotificationDeliveryChannel getChannel() { return channel; }
    public void setChannel(NotificationDeliveryChannel channel) { this.channel = channel; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public NotificationLogStatus getStatus() { return status; }
    public void setStatus(NotificationLogStatus status) { this.status = status; }
    public Map<String, Object> getProviderResponse() { return providerResponse; }
    public void setProviderResponse(Map<String, Object> providerResponse) { this.providerResponse = providerResponse; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
