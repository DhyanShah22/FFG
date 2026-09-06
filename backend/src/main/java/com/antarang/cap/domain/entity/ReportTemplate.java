package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.ReportTemplateType;
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

import java.util.UUID;

@Entity
@Table(
        name = "report_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "code"})
)
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 50)
    private ReportTemplateType templateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LifecycleStatus status = LifecycleStatus.DRAFT;

    @Column(name = "current_version_id")
    private UUID currentVersionId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ReportTemplateType getTemplateType() { return templateType; }
    public void setTemplateType(ReportTemplateType templateType) { this.templateType = templateType; }
    public LifecycleStatus getStatus() { return status; }
    public void setStatus(LifecycleStatus status) { this.status = status; }
    public UUID getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(UUID currentVersionId) { this.currentVersionId = currentVersionId; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
