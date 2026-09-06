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
        name = "careers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"career_cluster_id", "code"})
)
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_cluster_id", nullable = false)
    private CareerCluster careerCluster;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "education_pathway", columnDefinition = "TEXT")
    private String educationPathway;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills_required", columnDefinition = "jsonb")
    private Map<String, Object> skillsRequired;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public CareerCluster getCareerCluster() { return careerCluster; }
    public void setCareerCluster(CareerCluster careerCluster) { this.careerCluster = careerCluster; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEducationPathway() { return educationPathway; }
    public void setEducationPathway(String educationPathway) { this.educationPathway = educationPathway; }
    public Map<String, Object> getSkillsRequired() { return skillsRequired; }
    public void setSkillsRequired(Map<String, Object> skillsRequired) { this.skillsRequired = skillsRequired; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
