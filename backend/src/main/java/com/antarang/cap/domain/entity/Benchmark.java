package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.BenchmarkLabel;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "benchmarks")
public class Benchmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "domain_code", nullable = false, length = 100)
    private String domainCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_config_id")
    private Configuration gradeConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "age_group_config_id")
    private Configuration ageGroupConfig;

    @Column(name = "min_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal minScore;

    @Column(name = "max_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "benchmark_label", nullable = false, length = 100)
    private BenchmarkLabel benchmarkLabel;

    @Column(columnDefinition = "TEXT")
    private String interpretation;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public String getDomainCode() { return domainCode; }
    public void setDomainCode(String domainCode) { this.domainCode = domainCode; }
    public Configuration getGradeConfig() { return gradeConfig; }
    public void setGradeConfig(Configuration gradeConfig) { this.gradeConfig = gradeConfig; }
    public Configuration getAgeGroupConfig() { return ageGroupConfig; }
    public void setAgeGroupConfig(Configuration ageGroupConfig) { this.ageGroupConfig = ageGroupConfig; }
    public BigDecimal getMinScore() { return minScore; }
    public void setMinScore(BigDecimal minScore) { this.minScore = minScore; }
    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }
    public BenchmarkLabel getBenchmarkLabel() { return benchmarkLabel; }
    public void setBenchmarkLabel(BenchmarkLabel benchmarkLabel) { this.benchmarkLabel = benchmarkLabel; }
    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
