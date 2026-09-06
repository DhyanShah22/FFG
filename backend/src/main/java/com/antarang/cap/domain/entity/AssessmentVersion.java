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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "assessment_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assessment_id", "version_number"})
)
public class AssessmentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_version_id", nullable = false)
    private QuestionnaireVersion questionnaireVersion;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VersionStatus status = VersionStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public QuestionnaireVersion getQuestionnaireVersion() { return questionnaireVersion; }
    public void setQuestionnaireVersion(QuestionnaireVersion questionnaireVersion) { this.questionnaireVersion = questionnaireVersion; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public VersionStatus getStatus() { return status; }
    public void setStatus(VersionStatus status) { this.status = status; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
