package com.antarang.cap.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "report_sections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"report_template_version_id", "section_code"})
)
public class ReportSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_template_version_id", nullable = false)
    private ReportTemplateVersion reportTemplateVersion;

    @Column(name = "section_code", nullable = false, length = 100)
    private String sectionCode;

    @Column(name = "section_title", nullable = false, length = 200)
    private String sectionTitle;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "section_config", columnDefinition = "jsonb")
    private Map<String, Object> sectionConfig;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ReportTemplateVersion getReportTemplateVersion() { return reportTemplateVersion; }
    public void setReportTemplateVersion(ReportTemplateVersion reportTemplateVersion) { this.reportTemplateVersion = reportTemplateVersion; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public Map<String, Object> getSectionConfig() { return sectionConfig; }
    public void setSectionConfig(Map<String, Object> sectionConfig) { this.sectionConfig = sectionConfig; }
}
