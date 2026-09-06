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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "assessment_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"})
)
public class AssessmentResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "response_numeric", precision = 10, scale = 2)
    private BigDecimal responseNumeric;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_json", columnDefinition = "jsonb")
    private Map<String, Object> responseJson;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt = Instant.now();

    @Column(name = "is_skipped", nullable = false)
    private boolean isSkipped = false;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public void setAttempt(AssessmentAttempt attempt) { this.attempt = attempt; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }
    public BigDecimal getResponseNumeric() { return responseNumeric; }
    public void setResponseNumeric(BigDecimal responseNumeric) { this.responseNumeric = responseNumeric; }
    public Map<String, Object> getResponseJson() { return responseJson; }
    public void setResponseJson(Map<String, Object> responseJson) { this.responseJson = responseJson; }
    public Instant getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Instant answeredAt) { this.answeredAt = answeredAt; }
    public boolean isSkipped() { return isSkipped; }
    public void setSkipped(boolean skipped) { isSkipped = skipped; }
}
