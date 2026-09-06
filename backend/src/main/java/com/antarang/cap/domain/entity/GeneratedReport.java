package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.ReportType;
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
@Table(name = "generated_reports")
public class GeneratedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_run_id", nullable = false)
    private RecommendationRun recommendationRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_template_version_id", nullable = false)
    private ReportTemplateVersion reportTemplateVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuration_group_id", nullable = false)
    private AssessmentConfigurationGroup configurationGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "s3_key", nullable = false, columnDefinition = "TEXT")
    private String s3Key;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt = Instant.now();

    @Column(name = "generated_by")
    private UUID generatedBy;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public RecommendationRun getRecommendationRun() { return recommendationRun; }
    public void setRecommendationRun(RecommendationRun recommendationRun) { this.recommendationRun = recommendationRun; }
    public ReportTemplateVersion getReportTemplateVersion() { return reportTemplateVersion; }
    public void setReportTemplateVersion(ReportTemplateVersion reportTemplateVersion) { this.reportTemplateVersion = reportTemplateVersion; }
    public AssessmentConfigurationGroup getConfigurationGroup() { return configurationGroup; }
    public void setConfigurationGroup(AssessmentConfigurationGroup configurationGroup) { this.configurationGroup = configurationGroup; }
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public UUID getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(UUID generatedBy) { this.generatedBy = generatedBy; }
}
