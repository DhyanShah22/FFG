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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "career_mappings")
public class CareerMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;

    @Column(name = "interest_domain", length = 100)
    private String interestDomain;

    @Column(name = "aptitude_domain", length = 100)
    private String aptitudeDomain;

    @Column(name = "reality_factor", length = 100)
    private String realityFactor;

    @Column(name = "aspiration_factor", length = 100)
    private String aspirationFactor;

    @Column(name = "min_score", precision = 10, scale = 2)
    private BigDecimal minScore;

    @Column(name = "max_score", precision = 10, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "mapping_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal mappingWeight = BigDecimal.ONE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mapping_config", columnDefinition = "jsonb")
    private Map<String, Object> mappingConfig;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public Career getCareer() { return career; }
    public void setCareer(Career career) { this.career = career; }
    public String getInterestDomain() { return interestDomain; }
    public void setInterestDomain(String interestDomain) { this.interestDomain = interestDomain; }
    public String getAptitudeDomain() { return aptitudeDomain; }
    public void setAptitudeDomain(String aptitudeDomain) { this.aptitudeDomain = aptitudeDomain; }
    public String getRealityFactor() { return realityFactor; }
    public void setRealityFactor(String realityFactor) { this.realityFactor = realityFactor; }
    public String getAspirationFactor() { return aspirationFactor; }
    public void setAspirationFactor(String aspirationFactor) { this.aspirationFactor = aspirationFactor; }
    public BigDecimal getMinScore() { return minScore; }
    public void setMinScore(BigDecimal minScore) { this.minScore = minScore; }
    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }
    public BigDecimal getMappingWeight() { return mappingWeight; }
    public void setMappingWeight(BigDecimal mappingWeight) { this.mappingWeight = mappingWeight; }
    public Map<String, Object> getMappingConfig() { return mappingConfig; }
    public void setMappingConfig(Map<String, Object> mappingConfig) { this.mappingConfig = mappingConfig; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
