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
        name = "recommendations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"recommendation_run_id", "career_id"})
)
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_run_id", nullable = false)
    private RecommendationRun recommendationRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;

    @Column(name = "rank_order", nullable = false)
    private int rankOrder;

    @Column(name = "recommendation_score", precision = 10, scale = 2)
    private BigDecimal recommendationScore;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "match_breakdown", columnDefinition = "jsonb")
    private Map<String, Object> matchBreakdown;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public RecommendationRun getRecommendationRun() { return recommendationRun; }
    public void setRecommendationRun(RecommendationRun recommendationRun) { this.recommendationRun = recommendationRun; }
    public Career getCareer() { return career; }
    public void setCareer(Career career) { this.career = career; }
    public int getRankOrder() { return rankOrder; }
    public void setRankOrder(int rankOrder) { this.rankOrder = rankOrder; }
    public BigDecimal getRecommendationScore() { return recommendationScore; }
    public void setRecommendationScore(BigDecimal recommendationScore) { this.recommendationScore = recommendationScore; }
    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }
    public Map<String, Object> getMatchBreakdown() { return matchBreakdown; }
    public void setMatchBreakdown(Map<String, Object> matchBreakdown) { this.matchBreakdown = matchBreakdown; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
