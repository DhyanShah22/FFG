package com.antarang.cap.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Translation payload for a career in a specific language.
 *
 * <p>{@code careerId} and {@code languageId} reference the future
 * {@code careers} and {@code languages} tables, which do not exist yet. They are
 * mapped as plain UUID columns (rather than {@code @ManyToOne}) until those base
 * entities are introduced.</p>
 */
@Entity
@Table(
        name = "career_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"career_id", "language_id"})
)
public class CareerTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "career_id", nullable = false)
    private UUID careerId;

    @Column(name = "language_id", nullable = false)
    private UUID languageId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "education_pathway", columnDefinition = "TEXT")
    private String educationPathway;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCareerId() {
        return careerId;
    }

    public void setCareerId(UUID careerId) {
        this.careerId = careerId;
    }

    public UUID getLanguageId() {
        return languageId;
    }

    public void setLanguageId(UUID languageId) {
        this.languageId = languageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEducationPathway() {
        return educationPathway;
    }

    public void setEducationPathway(String educationPathway) {
        this.educationPathway = educationPathway;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
