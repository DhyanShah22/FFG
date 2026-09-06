package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.RuleType;
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

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "question_rules")
public class QuestionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private RuleType ruleType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> ruleConfig;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }
    public Map<String, Object> getRuleConfig() { return ruleConfig; }
    public void setRuleConfig(Map<String, Object> ruleConfig) { this.ruleConfig = ruleConfig; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
