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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "domain_scores",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "domain_code"})
)
public class DomainScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "domain_code", nullable = false, length = 100)
    private String domainCode;

    @Column(name = "raw_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal rawScore;

    @Column(name = "normalized_score", precision = 10, scale = 2)
    private BigDecimal normalizedScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benchmark_id")
    private Benchmark benchmark;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public void setAttempt(AssessmentAttempt attempt) { this.attempt = attempt; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public String getDomainCode() { return domainCode; }
    public void setDomainCode(String domainCode) { this.domainCode = domainCode; }
    public BigDecimal getRawScore() { return rawScore; }
    public void setRawScore(BigDecimal rawScore) { this.rawScore = rawScore; }
    public BigDecimal getNormalizedScore() { return normalizedScore; }
    public void setNormalizedScore(BigDecimal normalizedScore) { this.normalizedScore = normalizedScore; }
    public Benchmark getBenchmark() { return benchmark; }
    public void setBenchmark(Benchmark benchmark) { this.benchmark = benchmark; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
