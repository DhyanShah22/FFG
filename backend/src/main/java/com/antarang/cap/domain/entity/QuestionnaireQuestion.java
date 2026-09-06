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
        name = "questionnaire_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"questionnaire_version_id", "question_id"})
)
public class QuestionnaireQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_version_id", nullable = false)
    private QuestionnaireVersion questionnaireVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_mandatory", nullable = false)
    private boolean isMandatory = true;

    @Column(name = "section_code", length = 100)
    private String sectionCode;

    @Column(name = "section_name", length = 150)
    private String sectionName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_config", columnDefinition = "jsonb")
    private Map<String, Object> conditionConfig;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public QuestionnaireVersion getQuestionnaireVersion() { return questionnaireVersion; }
    public void setQuestionnaireVersion(QuestionnaireVersion questionnaireVersion) { this.questionnaireVersion = questionnaireVersion; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isMandatory() { return isMandatory; }
    public void setMandatory(boolean mandatory) { isMandatory = mandatory; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public Map<String, Object> getConditionConfig() { return conditionConfig; }
    public void setConditionConfig(Map<String, Object> conditionConfig) { this.conditionConfig = conditionConfig; }
}
