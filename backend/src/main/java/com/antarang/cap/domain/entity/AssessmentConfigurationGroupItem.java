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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "assessment_configuration_group_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"configuration_group_id", "assessment_id"})
)
public class AssessmentConfigurationGroupItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuration_group_id", nullable = false)
    private AssessmentConfigurationGroup configurationGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_version_id", nullable = false)
    private QuestionnaireVersion questionnaireVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_version_id", nullable = false)
    private AssessmentVersion assessmentVersion;

    @Column(name = "iar_logic_version_id")
    private UUID iarLogicVersionId;

    @Column(name = "scoring_rule_version_id")
    private UUID scoringRuleVersionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_language_id", nullable = false)
    private Language assessmentLanguage;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssessmentConfigurationGroup getConfigurationGroup() { return configurationGroup; }
    public void setConfigurationGroup(AssessmentConfigurationGroup configurationGroup) { this.configurationGroup = configurationGroup; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public QuestionnaireVersion getQuestionnaireVersion() { return questionnaireVersion; }
    public void setQuestionnaireVersion(QuestionnaireVersion questionnaireVersion) { this.questionnaireVersion = questionnaireVersion; }
    public AssessmentVersion getAssessmentVersion() { return assessmentVersion; }
    public void setAssessmentVersion(AssessmentVersion assessmentVersion) { this.assessmentVersion = assessmentVersion; }
    public UUID getIarLogicVersionId() { return iarLogicVersionId; }
    public void setIarLogicVersionId(UUID iarLogicVersionId) { this.iarLogicVersionId = iarLogicVersionId; }
    public UUID getScoringRuleVersionId() { return scoringRuleVersionId; }
    public void setScoringRuleVersionId(UUID scoringRuleVersionId) { this.scoringRuleVersionId = scoringRuleVersionId; }
    public Language getAssessmentLanguage() { return assessmentLanguage; }
    public void setAssessmentLanguage(Language assessmentLanguage) { this.assessmentLanguage = assessmentLanguage; }
    public boolean isRequired() { return isRequired; }
    public void setRequired(boolean required) { isRequired = required; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
