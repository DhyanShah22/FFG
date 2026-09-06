package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.NotificationDeliveryChannel;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationDeliveryChannel channel;

    @Column(name = "subject_template", columnDefinition = "TEXT")
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public NotificationDeliveryChannel getChannel() { return channel; }
    public void setChannel(NotificationDeliveryChannel channel) { this.channel = channel; }
    public String getSubjectTemplate() { return subjectTemplate; }
    public void setSubjectTemplate(String subjectTemplate) { this.subjectTemplate = subjectTemplate; }
    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
