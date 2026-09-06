package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.domain.enums.DifficultyLevel;
import com.antarang.cap.domain.enums.QuestionType;
import com.antarang.cap.domain.enums.ReviewStatus;
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

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 50)
    private AssessmentType assessmentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private QuestionCategory category;

    @Column(name = "question_code", nullable = false, length = 100)
    private String questionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    private QuestionType questionType;

    @Column(name = "default_text", nullable = false, columnDefinition = "TEXT")
    private String defaultText;

    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 50)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 50)
    private ReviewStatus reviewStatus = ReviewStatus.DRAFT;

    @Column(name = "source_reference", columnDefinition = "TEXT")
    private String sourceReference;

    @Column(name = "is_required_default", nullable = false)
    private boolean isRequiredDefault = true;

    @Column(name = "scoring_enabled", nullable = false)
    private boolean scoringEnabled = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public AssessmentType getAssessmentType() { return assessmentType; }
    public void setAssessmentType(AssessmentType assessmentType) { this.assessmentType = assessmentType; }
    public QuestionCategory getCategory() { return category; }
    public void setCategory(QuestionCategory category) { this.category = category; }
    public String getQuestionCode() { return questionCode; }
    public void setQuestionCode(String questionCode) { this.questionCode = questionCode; }
    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
    public String getDefaultText() { return defaultText; }
    public void setDefaultText(String defaultText) { this.defaultText = defaultText; }
    public String getHelpText() { return helpText; }
    public void setHelpText(String helpText) { this.helpText = helpText; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public ReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(ReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }
    public boolean isRequiredDefault() { return isRequiredDefault; }
    public void setRequiredDefault(boolean requiredDefault) { isRequiredDefault = requiredDefault; }
    public boolean isScoringEnabled() { return scoringEnabled; }
    public void setScoringEnabled(boolean scoringEnabled) { this.scoringEnabled = scoringEnabled; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
