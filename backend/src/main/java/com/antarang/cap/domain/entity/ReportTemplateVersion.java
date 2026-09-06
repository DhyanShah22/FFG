package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.VersionStatus;
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
        name = "report_template_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"report_template_id", "version_number"})
)
public class ReportTemplateVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_template_id", nullable = false)
    private ReportTemplate reportTemplate;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> templateConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VersionStatus status = VersionStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by")
    private UUID publishedBy;

    @Column(name = "version_notes", columnDefinition = "TEXT")
    private String versionNotes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ReportTemplate getReportTemplate() { return reportTemplate; }
    public void setReportTemplate(ReportTemplate reportTemplate) { this.reportTemplate = reportTemplate; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public Map<String, Object> getTemplateConfig() { return templateConfig; }
    public void setTemplateConfig(Map<String, Object> templateConfig) { this.templateConfig = templateConfig; }
    public VersionStatus getStatus() { return status; }
    public void setStatus(VersionStatus status) { this.status = status; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public UUID getPublishedBy() { return publishedBy; }
    public void setPublishedBy(UUID publishedBy) { this.publishedBy = publishedBy; }
    public String getVersionNotes() { return versionNotes; }
    public void setVersionNotes(String versionNotes) { this.versionNotes = versionNotes; }
}
