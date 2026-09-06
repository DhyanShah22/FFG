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
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "question_options",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "option_code"})
)
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_code", nullable = false, length = 100)
    private String optionCode;

    @Column(name = "default_text", nullable = false, columnDefinition = "TEXT")
    private String defaultText;

    @Column(name = "score_value", precision = 10, scale = 2)
    private BigDecimal scoreValue;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getOptionCode() { return optionCode; }
    public void setOptionCode(String optionCode) { this.optionCode = optionCode; }
    public String getDefaultText() { return defaultText; }
    public void setDefaultText(String defaultText) { this.defaultText = defaultText; }
    public BigDecimal getScoreValue() { return scoreValue; }
    public void setScoreValue(BigDecimal scoreValue) { this.scoreValue = scoreValue; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
