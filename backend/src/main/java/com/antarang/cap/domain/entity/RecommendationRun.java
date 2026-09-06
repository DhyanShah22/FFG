package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.RecommendationRunStatus;
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
@Table(name = "recommendation_runs")
public class RecommendationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuration_group_id", nullable = false)
    private AssessmentConfigurationGroup configurationGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iar_logic_version_id", nullable = false)
    private IarLogicVersion iarLogicVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecommendationRunStatus status = RecommendationRunStatus.PENDING;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt = Instant.now();

    @Column(name = "generated_by")
    private UUID generatedBy;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public AssessmentConfigurationGroup getConfigurationGroup() { return configurationGroup; }
    public void setConfigurationGroup(AssessmentConfigurationGroup configurationGroup) { this.configurationGroup = configurationGroup; }
    public IarLogicVersion getIarLogicVersion() { return iarLogicVersion; }
    public void setIarLogicVersion(IarLogicVersion iarLogicVersion) { this.iarLogicVersion = iarLogicVersion; }
    public RecommendationRunStatus getStatus() { return status; }
    public void setStatus(RecommendationRunStatus status) { this.status = status; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public UUID getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(UUID generatedBy) { this.generatedBy = generatedBy; }
}
