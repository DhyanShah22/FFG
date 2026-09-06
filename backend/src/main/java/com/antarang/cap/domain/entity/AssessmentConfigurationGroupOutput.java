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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "assessment_configuration_group_outputs")
public class AssessmentConfigurationGroupOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuration_group_id", nullable = false)
    private AssessmentConfigurationGroup configurationGroup;

    @Column(name = "report_template_version_id")
    private UUID reportTemplateVersionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "output_language_id", nullable = false)
    private Language outputLanguage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_config", columnDefinition = "jsonb")
    private Map<String, Object> outputConfig;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssessmentConfigurationGroup getConfigurationGroup() { return configurationGroup; }
    public void setConfigurationGroup(AssessmentConfigurationGroup configurationGroup) { this.configurationGroup = configurationGroup; }
    public UUID getReportTemplateVersionId() { return reportTemplateVersionId; }
    public void setReportTemplateVersionId(UUID reportTemplateVersionId) { this.reportTemplateVersionId = reportTemplateVersionId; }
    public Language getOutputLanguage() { return outputLanguage; }
    public void setOutputLanguage(Language outputLanguage) { this.outputLanguage = outputLanguage; }
    public Map<String, Object> getOutputConfig() { return outputConfig; }
    public void setOutputConfig(Map<String, Object> outputConfig) { this.outputConfig = outputConfig; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
